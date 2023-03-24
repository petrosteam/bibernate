package com.petros.bibernate.session.context;

import java.util.Optional;

/**
 * Holds the state of the persistence context, including the  <b>1st level cache</b> and <b>snapshot</b>.
 * Every Persistence Context is connected to the {@link com.petros.bibernate.dao.EntityPersister}.
 *
 * @see EntityKey
 */
public interface PersistenceContext {
    /**
     * This method is using for retrieving caching entities by entity type and id
     *
     * @param entityType entity class
     * @param id         entity id
     * @param <T>        type of entity
     * @return optional with entity object. If entity was not found in the cache, empty optional is returned
     */
    <T> Optional<T> getCachedEntity(Class<T> entityType, Object id);

    /**
     * Adding entity to cache. If entity with this {@link EntityKey} exists, an old entity is returned from cache and
     * new one is ignored.
     *
     * @param entity Bibernate entity
     * @param <T>    entity type
     * @return cached entity
     */
    <T> T cache(T entity);

    /**
     * Adding entity column values to snapshot
     *
     * @param entity Bibernate entity
     * @param <T>    entity type
     */
    <T> void snapshot(T entity);

    /**
     * Removing entity from context
     * @param entity Bibernate entity
     * @param <T> entity type
     */
    <T> void remove(T entity);

    /**
     * Clearing entity cache and snapshots
     */
    void clear();
}
