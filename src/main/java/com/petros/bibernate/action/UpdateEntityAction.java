package com.petros.bibernate.action;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;

/**
 * Represents an update action for an entity.
 */
@RequiredArgsConstructor
@Slf4j
public class UpdateEntityAction implements EntityAction {

    private final EntityPersister persister;
    private final Object entity;

    @Override
    public void execute(Connection connection, PersistenceContext persistenceContext) {
        log.trace("Updating entity: {}", entity);
        persister.update(this.entity, connection);
        log.trace("Entity updated: {}", entity);
    }

    @Override
    public int priority() {
        return EntityActionPriority.UPDATE.getPriority();
    }
}
