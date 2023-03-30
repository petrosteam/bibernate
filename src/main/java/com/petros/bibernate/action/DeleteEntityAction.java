package com.petros.bibernate.action;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;

@Builder
@RequiredArgsConstructor
public class DeleteEntityAction implements EntityAction {

    private final EntityPersister persister;
    private final Object entity;

    @Override
    public void execute(Connection connection, PersistenceContext persistenceContext) {
        Object entity = persister.delete(this.entity, connection);
        persistenceContext.remove(entity);
    }

    @Override
    public int priority() {
        return EntityActionPriority.DELETE.getPriority();
    }
}
