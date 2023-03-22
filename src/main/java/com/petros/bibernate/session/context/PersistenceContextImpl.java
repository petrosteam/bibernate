package com.petros.bibernate.session.context;

import com.petros.bibernate.util.EntityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersistenceContextImpl implements PersistenceContext {
    Map<EntityKey, Object> entityCache;

    Map<EntityKey, Object[]> snapshot;

    public PersistenceContextImpl() {
        this.entityCache = new HashMap<>();
        this.snapshot = new HashMap<>();
    }

    @Override
    public <T> Optional<T> getCachedEntity(Class<T> entityType, Object id) {
        return Optional.ofNullable(entityCache.get(EntityKey.of(entityType, id)))
                .map(entityType::cast);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T cache(T entity) {
        var key = this.getKey(entity);
        return (T) Optional.ofNullable(entityCache.putIfAbsent(key, entity))
                .orElse(entity);
    }

    @Override
    public <T> void snapshot(T entity, Object[] values) {
        var key = this.getKey(entity);
        snapshot.put(key, values);
    }

    @Override
    public <T> void remove(T entity) {
        var key = this.getKey(entity);
        this.snapshot.remove(key);
        this.entityCache.remove(key);
    }

    @Override
    public void clear() {
        this.entityCache.clear();
        this.snapshot.clear();
    }

    private <T> EntityKey getKey(T entity) {
        var id = EntityUtil.getIdValue(entity);
        return EntityKey.of(entity.getClass(), id);
    }
}
