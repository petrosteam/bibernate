package com.petros.bibernate.session;

import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.context.PersistenceContext;
import com.petros.bibernate.session.context.PersistenceContextImpl;

import javax.sql.DataSource;

public class SessionImpl implements Session {
    private boolean isOpened = true;
    private final EntityPersister entityPersister;

    private final PersistenceContext persistenceContext;

    public SessionImpl(DataSource dataSource, Configuration configuration) {
        this.entityPersister = new EntityPersister(dataSource, configuration.showSql());
        this.persistenceContext = new PersistenceContextImpl();
    }

    @Override
    public void flush() throws BibernateException {
        checkSession();
        persistenceContext.getSnapshotDiff().forEach(entityPersister::update);
    }

    @Override
    public <T> void persist(T object) {
        checkSession();
        persistenceContext.cache(entityPersister.insert(object));
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        checkSession();
        return persistenceContext.getCachedEntity(entityClass, primaryKey)
                .orElseGet(() -> persistenceContext.cache(entityPersister.findById(entityClass, primaryKey)));
    }

    @Override
    public <T> void remove(T entity) {
        checkSession();
        entityPersister.delete(entity);
        persistenceContext.remove(entity);
    }

    @Override
    public void close() {
        checkSession();
        flush();
        persistenceContext.clear();
        isOpened = false;
    }

    private void checkSession() {
        if (!isOpened) {
            throw new BibernateException("Session has been closed");
        }
    }
}
