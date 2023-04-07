package com.petros.bibernate.action;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;

/**
 * Represents a delete action for an entity.
 */
@Builder
@RequiredArgsConstructor
@Slf4j
public class DeleteEntityAction implements EntityAction {

    private final EntityPersister persister;
    private final Object entity;

    @Override
    public void execute(Connection connection, PersistenceContext persistenceContext) {
        log.trace("Deleting entity: {}", entity);
        Object entity = persister.delete(this.entity, connection);
        persistenceContext.remove(entity);
        log.trace("Entity deleted: {}", entity);
    }

    @Override
    public int priority() {
        return EntityActionPriority.DELETE.getPriority();
    }
}
