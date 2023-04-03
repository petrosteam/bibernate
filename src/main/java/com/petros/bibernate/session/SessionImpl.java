package com.petros.bibernate.session;

import com.petros.bibernate.action.DeleteEntityAction;
import com.petros.bibernate.action.EntityAction;
import com.petros.bibernate.action.InsertEntityAction;
import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.context.PersistenceContext;
import com.petros.bibernate.session.context.PersistenceContextImpl;
import com.petros.bibernate.util.EntityUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

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

    public SessionImpl(DataSource dataSource, EntityPersister entityPersister) {
        this.entityPersister = entityPersister;
        this.dataSource = dataSource;
        this.persistenceContext = new PersistenceContextImpl();
        this.actionQueue = new PriorityQueue<>(Comparator.comparing(EntityAction::priority));
        this.transaction = new TransactionImpl(this);
    }

    @Override
    public void flush() throws BibernateException {
        requireOpenSession();
        openConnection();
        try {
            setAutoCommitIfTxOpen(FALSE);
            actionQueue.forEach(action -> action.execute(connection, persistenceContext));
            actionQueue.clear();
            connection.commit();
            setAutoCommitIfTxOpen(TRUE);
        } catch (Exception ex) {
            try {
                connection.rollback();
                throw new BibernateException("Exception occurred during committing data", ex);
            } catch (SQLException e) {
                throw new BibernateException("Exception occurred during connection.rollback()", e);
            }
        }

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
        requireTransientState(entity);
        // TODO: 02.04.2023 fetch an id from database before storing entity into queue
        persistenceContext.cache(entity);
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
    public <T> List<T> findAll(Class<T> entityClass) {
        requireOpenSession();
        flush();
        return entityPersister.findAll(entityClass, connection);
    }

    @Override
    public <T> void remove(T entity) {
        requireOpenSession();
        requireOpenTransaction();
        requirePersistentState(entity);
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
        closeConnection();
    }

    @Override
    public void clear() {
        persistenceContext.clear();
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

    // An entity is in transient state in two cases:
    // 1) entity id is null
    // 2) if entity id is not null, then persistence context should not contain an entity with such id
    private <T> void requireTransientState(T entity) {
        ofNullable(EntityUtil.getIdValue(entity))
                .ifPresent(id -> {
                    persistenceContext.getCachedEntity(entity.getClass(), id)
                            .ifPresent(e -> {
                                throw new BibernateException(format("Entity %s must be in transient state. An id value [%s] must not exist", entity.getClass(), id));
                            });
                });

    }

    // An entity is in persistent state only when the entity id is not null, and it is present in persistence context
    private <T> void requirePersistentState(T entity) {
        ofNullable(EntityUtil.getIdValue(entity))
                .flatMap(id -> persistenceContext.getCachedEntity(entity.getClass(), id))
                .orElseThrow(() -> new BibernateException(format("Entity %s must be in persistent state (entity must be associated with persistence context)", entity.getClass())));
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

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw new BibernateException("Could not close database connection", ex);
            }
        }
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }
}
