package com.petros.bibernate.session;

import com.petros.bibernate.action.DeleteEntityAction;
import com.petros.bibernate.action.EntityAction;
import com.petros.bibernate.action.InsertEntityAction;
import com.petros.bibernate.action.UpdateEntityAction;
import com.petros.bibernate.annotation.FetchType;
import com.petros.bibernate.annotation.OneToMany;
import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.dao.lazy.LazyList;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.context.PersistenceContext;
import com.petros.bibernate.session.context.PersistenceContextImpl;
import com.petros.bibernate.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Field;
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

/**
 * Implementation of the {@link Session} interface.
 */
@Slf4j
public class SessionImpl implements Session {
    private final Transaction transaction;
    private final DataSource dataSource;
    private final EntityPersister entityPersister;
    private final Queue<EntityAction> actionQueue;
    private final PersistenceContext persistenceContext;
    private boolean isOpened = true;
    private Connection connection;

    public SessionImpl(DataSource dataSource, Configuration configuration) {
        log.info("Creating SessionImpl instance with dataSource and configuration");
        this.entityPersister = new EntityPersister(configuration.showSql());
        this.dataSource = dataSource;
        this.persistenceContext = new PersistenceContextImpl();
        this.actionQueue = new PriorityQueue<>(Comparator.comparing(EntityAction::priority));
        this.transaction = new TransactionImpl(this);
    }

    public SessionImpl(DataSource dataSource, EntityPersister entityPersister) {
        log.info("Creating SessionImpl instance with dataSource and entityPersister");
        this.entityPersister = entityPersister;
        this.dataSource = dataSource;
        this.persistenceContext = new PersistenceContextImpl();
        this.actionQueue = new PriorityQueue<>(Comparator.comparing(EntityAction::priority));
        this.transaction = new TransactionImpl(this);
    }

    @Override
    public void flush() throws BibernateException {
        log.info("Flushing session");
        requireOpenSession();
        openConnection();
        try {
            setAutoCommitIfTxOpen(FALSE);
            persistenceContext.getSnapshotDiff().forEach(entity -> actionQueue.add(new UpdateEntityAction(entityPersister,
                    entity)));
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
        log.trace("Persisting entity of class {}", entity.getClass());
        requireOpenSession();
        requireOpenTransaction();
        requireTransientState(entity);
        actionQueue.add(InsertEntityAction.builder()
                .entity(entity)
                .persister(entityPersister)
                .build());
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        log.trace("Finding entity of class {} with primary key {}", entityClass, primaryKey);
        requireOpenSession();
        flush();
        return persistenceContext.getCachedEntity(entityClass, primaryKey)
                .orElseGet(() -> {
                    T entity = persistenceContext.cache(entityPersister.findById(entityClass, primaryKey, connection));
                    initializeRelations(entityClass, entity);
                    return entity;
                });
    }

    private <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue, Connection connection) {
        log.trace("Finding all entities of class {} with field {} and value {}", entityClass, field, fieldValue);
        if (!isOpened) {
            throw new BibernateException(format("Could not lazily initialize field [%s] of class [%s]", field,
                    entityClass));
        }
        return entityPersister.findAll(entityClass, field, fieldValue, connection).stream()
                .map(entity ->
                        persistenceContext.getCachedEntity(entityClass, EntityUtil.getIdValue(entity))
                                .orElseGet(() -> persistenceContext.cache(entity)))
                .toList();
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        log.trace("Finding all entities of class {}", entityClass);
        requireOpenSession();
        flush();
        return entityPersister.findAll(entityClass, connection);
    }

    @Override
    public <T> void remove(T entity) {
        log.trace("Removing entity of class {}", entity.getClass());
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
        log.info("Closing session");
        requireOpenSession();
        flush();
        persistenceContext.clear();
        isOpened = false;
        closeConnection();
    }

    @Override
    public void clear() {
        log.info("Clearing session");
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
            throw new BibernateException("Transaction must be opened. Use session.openTransaction().begin() before " +
                    "persist or delete");
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
                                throw new BibernateException(format("Entity %s must be in transient state. An id " +
                                        "value [%s] must not exist", entity.getClass(), id));
                            });
                });
    }

    // An entity is in persistent state only when the entity id is not null, and it is present in persistence context
    private <T> void requirePersistentState(T entity) {
        ofNullable(EntityUtil.getIdValue(entity))
                .flatMap(id -> persistenceContext.getCachedEntity(entity.getClass(), id))
                .orElseThrow(() -> new BibernateException(format("Entity %s must be in persistent state (entity must " +
                        "be associated with persistence context)", entity.getClass())));
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

    private <T> void initializeRelations(Class<T> entityClass, T entity) {
        Field[] entityFields = EntityUtil.getEntityRelationFields(entityClass);
        for (var entityField : entityFields) {
            entityField.setAccessible(TRUE);
            if (EntityUtil.isEntityField(entityField)) {
                initializeEntityRelation(entity, entityField);
            } else if (EntityUtil.isEntityCollectionField(entityField)) {
                initializeCollectionRelation(entity, entityField);
            }
        }
    }

    private <T> void initializeEntityRelation(T entity, Field entityField) {
        try {
            var relatedEntity = entityField.get(entity);
            var relatedEntityClass = entityField.getType();
            var relatedEntityIdValue = EntityUtil.getIdValue(relatedEntity);
            var initializedEntity = find(relatedEntityClass, relatedEntityIdValue);
            entityField.set(entity, initializedEntity);
        } catch (IllegalAccessException e) {
            throw new BibernateException(format("Could not initialize field [%s] in entity [%s]", entityField,
                    entity), e);
        }
    }

    private <T> void initializeCollectionRelation(T entity, Field entityField) {
        try {
            var relatedEntityType = EntityUtil.getRelatedEntityType(entityField);
            var relatedEntityId = EntityUtil.getIdValue(entity);
            var ann = entityField.getAnnotation(OneToMany.class);
            var relatedEntityField = relatedEntityType.getDeclaredField(ann.mappedBy());
            var fetchType = ann.fetchType();
            if (fetchType.equals(FetchType.LAZY)) {
                var relatedEntityCollection = new LazyList<T>(() -> findAll(relatedEntityType, relatedEntityField,
                        relatedEntityId, connection));
                entityField.set(entity, relatedEntityCollection);
            } else {
                var relatedEntityCollection = findAll(relatedEntityType, relatedEntityField, relatedEntityId,
                        connection);
                entityField.set(entity, relatedEntityCollection);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BibernateException(format("Could not initialize field [%s] in entity [%s]", entityField,
                    entity), e);
        }
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }
}
