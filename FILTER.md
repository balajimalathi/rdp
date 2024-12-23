Supported Query Operators

    Equality (:)
        Matches exact values.
        Example: fieldName: 'value'

    Inequality (!)
        Matches values not equal to the specified value.
        Example: fieldName! 'value'

    Greater Than (>)
        Matches values greater than the specified value.
        Example: fieldName > 'value'

    Greater Than or Equal To (>:)
        Matches values greater than or equal to the specified value.
        Example: fieldName >: 'value'

    Less Than (<)
        Matches values less than the specified value.
        Example: fieldName < 'value'

    Less Than or Equal To (<:)
        Matches values less than or equal to the specified value.
        Example: fieldName <: 'value'

    Contains (~)
        Matches values containing the specified substring.
        Example: fieldName ~ 'substring'

    Null Check (is)
        Matches values that are null or not null.
        Examples:
            fieldName is null
            fieldName is not null

    Collection Membership (in and not in)
        Matches values present or not present in a collection.
        Examples:
            fieldName in ['value1', 'value2', 'value3']
            fieldName not in ['value1', 'value2']

    Logical Operators (and, or, not)
        Combines multiple conditions.
        Examples:
            fieldName: 'value' and fieldName2 > '10'
            fieldName: 'value' or fieldName2 <: '5'
            not fieldName: 'value'

Example Query Strings
Simple Queries

    Match by exact value:

name: 'John'

Match by inequality:

age! '30'

Match by greater than:

salary > '50000'

Match by substring:

department ~ 'HR'

Match by null:

    manager is null

Complex Queries

    Combine conditions with and:

name: 'John' and age > '25'

Combine conditions with or:

department: 'Finance' or department: 'HR'

Use not to negate a condition:

    not department: 'Finance'

Collection Queries

    Check if a field is in a list:

role in ['Admin', 'User']

Check if a field is not in a list:

    role not in ['Guest', 'External']

Nested Queries

    Combine multiple logical operators:

(name: 'John' or name: 'Jane') and age > '30'

Use not with complex conditions:

    not (department: 'Finance' or department: 'HR')

