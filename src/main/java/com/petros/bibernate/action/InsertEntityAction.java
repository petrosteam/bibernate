package com.petros.bibernate.action;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;


/**
 * Represents an insert action for an entity.
 */
@Builder
@RequiredArgsConstructor
@Slf4j
public class InsertEntityAction implements EntityAction {

    private final EntityPersister persister;
    private final Object entity;

    @Override
    public void execute(Connection connection, PersistenceContext persistenceContext) {
        log.trace("Inserting entity: {}", entity);
        Object entity = persister.insert(this.entity, connection);
        persistenceContext.cache(entity);
        log.trace("Entity inserted: {}", entity);

    }

    @Override
    public int priority() {
        return EntityActionPriority.INSERT.getPriority();
    }
}
