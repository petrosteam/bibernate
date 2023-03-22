package com.petros.bibernate.session;

import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.exception.BibernateException;

import javax.sql.DataSource;

public class SessionImpl implements Session {
    private final EntityPersister entityPersister;

    public SessionImpl(DataSource dataSource) {
        this.entityPersister = new EntityPersister(dataSource);
    }

    @Override
    public void flush() throws BibernateException {

    }

    @Override
    public <T> void persist(T object) {

    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return entityPersister.findById(entityClass, primaryKey);
    }

    @Override
    public <T> void remove(T entity) {

    }

    @Override
    public void close() {
        entityPersister.close();
    }
}
