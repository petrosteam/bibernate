package com.petros.bibernate.dao;

import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.petros.bibernate.util.EntityUtil.getColumnName;
import static com.petros.bibernate.util.EntityUtil.getIdField;
import static com.petros.bibernate.util.EntityUtil.getIdValue;
import static com.petros.bibernate.util.EntityUtil.getInsertableColumns;
import static com.petros.bibernate.util.EntityUtil.getInsertableValues;
import static com.petros.bibernate.util.EntityUtil.getTableName;
import static com.petros.bibernate.util.EntityUtil.getUpdatableColumns;
import static com.petros.bibernate.util.EntityUtil.getUpdatableValues;
import static java.lang.Boolean.TRUE;

/**
 * Provides basic functionality for persisting an entity via JDBC.
 */
@Slf4j
public class EntityPersister {
    private final DataSource dataSource;
    private static final String FIND_ENTITY_BY_FIELD_NAME_TEMPLATE = "select * from %s where %s = ?;";
    private static final String INSERT_INTO_TABLE_VALUES_TEMPLATE = "insert into %s(%s) values (%s);";
    private static final String UPDATE_BY_ID_TEMPLATE = "update %s set %s where %s = ?;";

    public EntityPersister(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Retrieves an entity by its identifier.
     *
     * @param entityClass the class of the entity to retrieve
     * @param idValue     the value of the identifier
     * @param <T>         the type of the entity
     * @return the entity with the specified identifier, or null if not found
     */
    public <T> T findById(Class<T> entityClass, Object idValue) {
        Field idField = getIdField(entityClass);
        log.debug("Searching for {} entity by field = {} with id = {}", entityClass.getSimpleName(), idField.getName(), idValue);
        return findOne(entityClass, idField, idValue);
    }

    /**
     * Retrieves a single entity based on a field and its value.
     *
     * @param entityClass the class of the entity to retrieve
     * @param field       the field to search by
     * @param fieldValue  the value of the field to search by
     * @param <T>         the type of the entity
     * @return the entity with the specified field value, or null if not found
     */
    public <T> T findOne(Class<T> entityClass, Field field, Object fieldValue) {
        List<T> entities = findAll(entityClass, field, fieldValue);
        if (entities.size() == 0) {
            return null;
        }
        if (entities.size() > 1) {
            throw new BibernateException("Result must contain exactly one row");
        }
        return entities.get(0);
    }

    /**
     * Retrieves all entities based on a field and its value.
     *
     * @param entityClass the class of the entity to retrieve
     * @param field       the field to search by
     * @param fieldValue  the value of the field to search by
     * @param <T>         the type of the entity
     * @return a list of entities with the specified field value
     */
    public <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue) {
        List<T> result = new ArrayList<>();

        try (var connection = dataSource.getConnection()) {
            String tableName = getTableName(entityClass);
            String columnName = getColumnName(field);
            String query = String.format(FIND_ENTITY_BY_FIELD_NAME_TEMPLATE, tableName, columnName);

            try (var statement = connection.prepareStatement(query)) {
                statement.setObject(1, fieldValue);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    result.add(mapResultSetToEntity(entityClass, resultSet));
                }
            }
        } catch (SQLException e) {
            throw new BibernateException(e);
        }
        return result;
    }

    /**
     * Inserts a new entity into the database.
     *
     * @param entity the entity to insert
     * @param <T>    the type of the entity
     * @return the inserted entity with the generated identifier
     **/
    public <T> T insert(T entity) {
        Objects.requireNonNull(entity);
        try (var connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = prepareInsertStatement(entity, connection);

            int rowsAffected = insertStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to insert entity into the database");
            setIdFromGeneratedKeys(entity, insertStatement);
            return entity;

        } catch (SQLException | IllegalAccessException e) {
            throw new BibernateException(e);
        }
    }

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity to update
     * @param <T>    the type of the entity
     * @return the updated entity
     */
    public <T> T update(T entity) {
        Objects.requireNonNull(entity);
        try (var connection = dataSource.getConnection()) {
            PreparedStatement updateStatement = prepareUpdateStatement(entity, connection);

            int rowsAffected = updateStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to update entity in the database");
            return entity;

        } catch (SQLException | IllegalAccessException e) {
            throw new BibernateException(e);
        }
    }

    private static void throwExceptionIfRowsAffectedNotOne(int rowsAffected, String message) {
        if (rowsAffected != 1) {
            throw new BibernateException(message);
        }
    }

    private static <T> PreparedStatement prepareInsertStatement(T entity, Connection connection) throws SQLException {
        String tableName = getTableName(entity.getClass());
        List<String> columns = getInsertableColumns(entity.getClass());
        List<Object> values = getInsertableValues(entity);
        String insertPlaceHolders = getInsertPlaceholders(columns);
        String insertQuery = String.format(INSERT_INTO_TABLE_VALUES_TEMPLATE, tableName,
                String.join(", ", columns), insertPlaceHolders);

        PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        setPreparedStatementValues(values, statement);
        return statement;
    }

    private static <T> PreparedStatement prepareUpdateStatement(T entity, Connection connection) throws SQLException,
            IllegalAccessException {
        String tableName = getTableName(entity.getClass());
        Field idField = getIdField(entity.getClass());
        Object idValue = getIdValue(entity);
        if (idValue == null) {
            throw new BibernateException("ID field is null");
        }
        List<String> updateColumns = getUpdatableColumns(entity);
        List<Object> updateValues = getUpdatableValues(entity);

        String updateQuery = String.format(UPDATE_BY_ID_TEMPLATE, tableName,
                getUpdatePlaceholders(updateColumns), getColumnName(idField));
        PreparedStatement statement = connection.prepareStatement(updateQuery);
        setPreparedStatementValues(updateValues, statement);
        statement.setObject(updateValues.size() + 1, idValue);
        return statement;
    }

    private static <T> void setIdFromGeneratedKeys(T entity, PreparedStatement statement) throws SQLException,
            IllegalAccessException {
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            final Field field = getIdField(entity.getClass());
            field.setAccessible(true);
            field.set(entity, generatedKeys.getObject(1, getIdField(entity.getClass()).getType()));
        }
    }

    private static void setPreparedStatementValues(List<Object> values, PreparedStatement statement) throws SQLException {
        int i = 1;
        for (Object value : values) {
            statement.setObject(i++, value);
        }
    }

    private static String getInsertPlaceholders(List<?> insertableColumns) {
        return insertableColumns.stream()
                .map(f -> "?")
                .collect(Collectors.joining(","));
    }

    private static String getUpdatePlaceholders(List<?> updatableColumns) {
        return updatableColumns.stream()
                .map(column -> column + " = ?")
                .collect(Collectors.joining(", "));
    }

    private static <T> T mapResultSetToEntity(Class<T> entityClass, ResultSet resultSet) {
        try {
            T entity = entityClass.getConstructor().newInstance();
            for (var entityField : entityClass.getDeclaredFields()) {
                entityField.setAccessible(TRUE);
                String columnName = getColumnName(entityField);
                entityField.set(entity, resultSet.getObject(columnName));
            }
            return entity;
        } catch (InstantiationException e) {
            throw new BibernateException(e);
        } catch (IllegalAccessException e) {
            throw new BibernateException(e);
        } catch (InvocationTargetException e) {
            throw new BibernateException(e);
        } catch (NoSuchMethodException e) {
            throw new BibernateException(e);
        } catch (SQLException e) {
            throw new BibernateException(e);
        }
    }
}
