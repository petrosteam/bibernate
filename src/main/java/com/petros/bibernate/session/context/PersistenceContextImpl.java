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
        var id = EntityUtil.getIdFieldValue(entity);
        var key = EntityKey.of(entity.getClass(), id);
        return (T) Optional.ofNullable(entityCache.putIfAbsent(key, entity))
                .orElse(entity);
    }

    @Override
    public <T> void snapshot(T entity, Object[] values) {
        var id = EntityUtil.getIdFieldValue(entity);
        var key = EntityKey.of(entity.getClass(), id);
        snapshot.put(key, values);
    }

    @Override
    public void clear() {
        this.entityCache.clear();
        this.snapshot.clear();
    }
}
