package com.petros.bibernate.session.context;

/**
 * Uniquely identifies of an entity instance in a particular session by identifier.
 * Information consists of the entity-name and the identifier value.
 *
 * @param entityType entity type class
 * @param id         the user-visible identifier
 */
public record EntityKey(Class<?> entityType, Object id) {

    static EntityKey of(Class<?> entityType, Object id) {
        return new EntityKey(entityType, id);
    }
}
