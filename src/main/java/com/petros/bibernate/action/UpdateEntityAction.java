package com.petros.bibernate.action;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;

@RequiredArgsConstructor
public class UpdateEntityAction implements EntityAction {

    private final EntityPersister persister;
    private final Object entity;

    @Override
    public void execute(Connection connection, PersistenceContext persistenceContext) {
        Object entity = persister.update(this.entity, connection);
        persistenceContext.remove(entity);
        persistenceContext.cache(entity);
    }

    @Override
    public int priority() {
        return EntityActionPriority.UPDATE.getPriority();
    }
}
