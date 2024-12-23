package com.skndan.rdp.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

public class QueryBuilder<T> {

    private final EntityManager entityManager;
    private final Class<T> entityClass;

    public QueryBuilder(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    public static <T> QueryBuilder<T> create(EntityManager entityManager, Class<T> clazz) {
        return new QueryBuilder<>(entityManager, clazz);
    }

    public PaginatedResponse<T> build(String queryString, int pageNumber, int pageSize) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Build query for data
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        Predicate predicate = parsePredicate(queryString, cb, root);
        cq.where(predicate);

        TypedQuery<T> query = entityManager.createQuery(cq);
        query.setFirstResult(pageNumber * pageSize); // Starting position of the first result
        query.setMaxResults(pageSize); // Number of results per page

        List<T> items = query.getResultList();

        // Build query for count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot)).where(predicate);

        long totalItems = entityManager.createQuery(countQuery).getSingleResult();

        // Return paginated response
        return new PaginatedResponse<T>(items, pageNumber, pageSize, totalItems);
    }

    private Predicate parsePredicate(String queryString, CriteriaBuilder cb, Root<T> root) {
        String[] conditions = queryString.split(" and | or ");
        List<Predicate> predicates = new ArrayList<>();

        for (String condition : conditions) {
            predicates.add(parseCondition(condition.trim(), cb, root));
        }

        Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
        return finalPredicate;
    }

    private Predicate parseCondition(String condition, CriteriaBuilder cb, Root<T> root) {
        condition = condition.trim();

        // Handle NOT operator
        if (condition.startsWith("not ")) {
            String subCondition = condition.substring(4).trim();
            return cb.not(parseCondition(subCondition, cb, root));
        }

        // Check for placeholders and remove them
        if (condition.contains("`")) {
            String placeholder = condition.substring(condition.indexOf('`') + 1, condition.lastIndexOf('`'));
            // Replace placeholder logic here if needed
            condition = condition.replace("`" + placeholder + "`", ""); // Simplified example
        }

        // Handle collection membership (in/not in)
        if (condition.contains(" in ") || condition.contains(" not in ")) {
            return parseCollectionCondition(condition, cb, root);
        }

        // Split based on space to get field and operator
        String[] parts = condition.split(" ");

        if (parts.length >= 3) {
            String fieldPath = parts[0];
            String operator = parts[1];
            String value = condition.substring(condition.indexOf(operator) + operator.length()).trim().replace("'", "");

            Path<?> expression = getFieldExpression(root, fieldPath);

            switch (operator) {
                case ":":
                    return cb.equal(expression, value);
                case "!":
                    return cb.notEqual(expression, value);
                case ">":
                    return cb.greaterThan(expression.as(String.class), value); // Adjust type as necessary
                case ">:":
                    return cb.greaterThanOrEqualTo(expression.as(String.class), value);
                case "<":
                    return cb.lessThan(expression.as(String.class), value);
                case "<:":
                    return cb.lessThanOrEqualTo(expression.as(String.class), value);
                case "~": // Contains operator
                    return cb.like(cb.lower(expression.as(String.class)), "%" + value.toLowerCase() + "%");
                case "is":
                    if ("null".equals(value)) {
                        return cb.isNull(expression);
                    } else if ("not null".equals(value)) {
                        return cb.isNotNull(expression);
                    }
                    throw new IllegalArgumentException("Unsupported condition: " + condition);
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }

        throw new IllegalArgumentException("Invalid condition: " + condition);
    }

    private Predicate parseCollectionCondition(String condition, CriteriaBuilder cb, Root<T> root) {
        String[] parts;

        if (condition.contains(" in ")) {
            parts = condition.split("\\s+in\\s+");
            String fieldPath = parts[0].trim();
            String valuesString = parts[1].trim().replaceAll("\\[|\\]", ""); // Remove brackets
            String[] valuesArray = valuesString.split(",\\s*");

            Path<?> expression = getFieldExpression(root, fieldPath);
            CriteriaBuilder.In<Object> inClause = cb.in(expression);

            for (String value : valuesArray) {
                inClause.value(value.replace("'", ""));
            }

            return inClause;
        } else if (condition.contains(" not in ")) {
            parts = condition.split("\\s+not in\\s+");
            String fieldPath = parts[0].trim();
            String valuesString = parts[1].trim().replaceAll("\\[|\\]", "");
            String[] valuesArray = valuesString.split(",\\s*");

            Path<?> expression = getFieldExpression(root, fieldPath);
            CriteriaBuilder.In<Object> inClause = cb.in(expression);

            for (String value : valuesArray) {
                inClause.value(value.replace("'", ""));
            }

            return cb.not(inClause);
        }

        throw new IllegalArgumentException("Invalid collection condition: " + condition);
    }

    private Path<?> getFieldExpression(Root<T> root, String fieldPath) {
        Path<?> expression = root;

        for (String field : fieldPath.split("\\.")) {
            expression = expression.get(field);
        }

        return expression;
    }
}
