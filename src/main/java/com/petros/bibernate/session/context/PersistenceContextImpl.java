package com.petros.bibernate.session.context;

import com.petros.bibernate.util.EntityUtil;

import java.util.*;

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
        return (T) Optional.ofNullable(entity)
                .map(this::getKey)
                .map(key -> entityCache.putIfAbsent(key, entity))
                .or(() -> Optional.ofNullable(entity))
                .map(e -> {
                    this.snapshot(e);
                    return e;
                })
                .orElse(null);
    }

    @Override
    public <T> void snapshot(T entity) {
        var key = this.getKey(entity);
        var values = EntityUtil.getEntityFields(entity).toArray();
        snapshot.put(key, values);
    }

    @Override
    public <T> void remove(T entity) {
        var key = this.getKey(entity);
        this.snapshot.remove(key);
        this.entityCache.remove(key);
    }

    @Override
    public List<Object> getSnapshotDiff() {
        var diff = new ArrayList<>();
        for (var cachedEntry : entityCache.entrySet()) {
            var entity = cachedEntry.getValue();
            var key = cachedEntry.getKey();
            var entitySnapshot = snapshot.get(key);
            var entityColumns = EntityUtil.getEntityColumns(key.entityType());
            for (int i = 0; i < entityColumns.length; i++) {
                var field = entityColumns[i];
                field.setAccessible(true);
                var entityFieldValue = EntityUtil.getFieldValue(field, entity);
                var snapshotFieldValue = entitySnapshot[i];
                if (!snapshotFieldValue.equals(entityFieldValue)) {
                    diff.add(entity);
                }
            }
        }
        return diff;
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
