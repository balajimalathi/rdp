package com.skndan.rdp.repo;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public abstract class BaseRepo<T> {

    @Inject
    EntityManager em;

    private Class<T> entityClass;

    public BaseRepo(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public List<T> findByCriteria(Map<String, Object> filters, int page, int size, String sortField, String sortOrder) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        Predicate[] predicates = filters.entrySet().stream()
                .map(entry -> cb.equal(root.get(entry.getKey()), entry.getValue()))
                .toArray(Predicate[]::new);

        cq.where(predicates);

        if (sortField != null && !sortField.isEmpty()) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                cq.orderBy(cb.asc(root.get(sortField)));
            } else {
                cq.orderBy(cb.desc(root.get(sortField)));
            }
        }

        TypedQuery<T> query = em.createQuery(cq);

        if (page >= 0 && size > 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    // Additional methods like findById, save, delete could be added here as needed.
}