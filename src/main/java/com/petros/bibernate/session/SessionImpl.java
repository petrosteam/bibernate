package com.petros.bibernate.session;

import com.petros.bibernate.action.DeleteEntityAction;
import com.petros.bibernate.action.EntityAction;
import com.petros.bibernate.action.InsertEntityAction;
import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.context.PersistenceContext;
import com.petros.bibernate.session.context.PersistenceContextImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class SessionImpl implements Session {
    private boolean isOpened = true;
    private final Transaction transaction;
    private final DataSource dataSource;
    private final EntityPersister entityPersister;
    private final Queue<EntityAction> actionQueue;
    private final PersistenceContext persistenceContext;
    private Connection connection;

    public SessionImpl(DataSource dataSource, Configuration configuration) {
        this.entityPersister = new EntityPersister(configuration.showSql());
        this.dataSource = dataSource;
        this.persistenceContext = new PersistenceContextImpl();
        this.actionQueue = new PriorityQueue<>(Comparator.comparing(EntityAction::priority));
        this.transaction = new TransactionImpl(this);
    }

    @Override
    public void flush() throws BibernateException {
        requireOpenSession();
        openConnection();
        setAutoCommitIfTxOpen(FALSE);
        actionQueue.forEach(action -> action.execute(connection, persistenceContext));
        actionQueue.clear();
        setAutoCommitIfTxOpen(TRUE);
    }

    private void setAutoCommitIfTxOpen(boolean isAutoCommit) {
        if (transaction.isOpen()) {
            try {
                connection.setAutoCommit(isAutoCommit);
            } catch (SQLException ex) {
                throw new BibernateException("Could not set autoCommit=false", ex);
            }
        }
    }

    @Override
    public <T> void persist(T entity) {
        requireOpenSession();
        requireOpenTransaction();
        actionQueue.add(InsertEntityAction.builder()
                .entity(entity)
                .persister(entityPersister)
                .build());
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        requireOpenSession();
        flush();
        return persistenceContext.getCachedEntity(entityClass, primaryKey)
                .orElseGet(() -> persistenceContext.cache(entityPersister.findById(entityClass, primaryKey, connection)));
    }

    @Override
    public <T> void remove(T entity) {
        requireOpenSession();
        requireOpenTransaction();
        actionQueue.add(DeleteEntityAction.builder()
                .entity(entity)
                .persister(entityPersister)
                .build());
    }

    @Override
    public void close() {
        persistenceContext.clear();
        actionQueue.clear();
        isOpened = false;
    }

    @Override
    public void clear() {
        actionQueue.clear();
    }

    private void requireOpenSession() {
        if (!isOpened) {
            throw new BibernateException("Session has been closed");
        }
    }

    private void requireOpenTransaction() {
        if (!transaction.isOpen()) {
            throw new BibernateException("Transaction must be opened. Use session.openTransaction().begin() before persist or delete");
        }
    }

    private void openConnection() {
        if (connection == null) {
            try {
                connection = dataSource.getConnection();
            } catch (SQLException ex) {
                throw new BibernateException("Could not open database connection", ex);
            }
        }
    }

    @Override
    public Transaction openTransaction() {
        return this.transaction;
    }
}
