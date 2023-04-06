package com.petros.bibernate.dao;

import com.petros.bibernate.annotation.FetchType;
import com.petros.bibernate.annotation.OneToMany;
import com.petros.bibernate.dao.lazy.LazyList;
import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.petros.bibernate.util.EntityUtil.getColumnName;
import static com.petros.bibernate.util.EntityUtil.getIdField;
import static com.petros.bibernate.util.EntityUtil.getIdValue;
import static com.petros.bibernate.util.EntityUtil.getInsertableColumns;
import static com.petros.bibernate.util.EntityUtil.getInsertableValues;
import static com.petros.bibernate.util.EntityUtil.getJoinColumnName;
import static com.petros.bibernate.util.EntityUtil.getTableName;
import static com.petros.bibernate.util.EntityUtil.getUpdatableColumns;
import static com.petros.bibernate.util.EntityUtil.getUpdatableValues;
import static com.petros.bibernate.util.EntityUtil.isEntityCollectionField;
import static com.petros.bibernate.util.EntityUtil.isEntityField;
import static com.petros.bibernate.util.EntityUtil.isRegularField;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Provides basic functionality for persisting an entity via JDBC.
 */
@Slf4j
public class EntityPersister {
    private final boolean showSql;
    private static final String FIND_ENTITY_BY_FIELD_NAME_TEMPLATE = "SELECT * FROM %s WHERE %s = ?;";
    private static final String FIND_ALL_ENTITIES_FROM_TABLE_TEMPLATE = "SELECT * FROM %s;";
    private static final String INSERT_INTO_TABLE_VALUES_TEMPLATE = "INSERT INTO %s(%s) VALUES (%s);";
    private static final String UPDATE_BY_ID_TEMPLATE = "UPDATE %s SET %s WHERE %s = ?;";
    private static final String DELETE_BY_ID_TEMPLATE = "DELETE FROM %s WHERE %s = ?;";

    public EntityPersister(boolean showSql) {
        this.showSql = showSql;
    }

    public EntityPersister() {
        this(FALSE);
    }

    /**
     * Retrieves an entity by its identifier.
     *
     * @param entityClass the class of the entity to retrieve
     * @param idValue     the value of the identifier
     * @param <T>         the type of the entity
     * @return the entity with the specified identifier, or null if not found
     */
    public <T> T findById(Class<T> entityClass, Object idValue, Connection connection) {
        Field idField = getIdField(entityClass);
        return this.findOne(entityClass, idField, idValue, connection);
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
    public <T> T findOne(Class<T> entityClass, Field field, Object fieldValue, Connection connection) {
        List<T> entities = findAll(entityClass, field, fieldValue, connection);
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
    public <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue, Connection connection) {
        List<T> result = new ArrayList<>();

        try (PreparedStatement statement = prepareFindStatement(entityClass, field, fieldValue, connection)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(mapResultSetToEntity(entityClass, resultSet, connection));
            }
        } catch (SQLException e) {
            throw new BibernateException(e);
        }
        return result;
    }

    /**
     * Retrieves all entities of specified entityClass.
     *
     * @param entityClass the class of the entity to retrieve
     * @param <T>         the type of the entity
     * @return a list of entities
     */
    public <T> List<T> findAll(Class<T> entityClass, Connection connection) {
        List<T> result = new ArrayList<>();

        try (PreparedStatement statement = prepareFindAllStatement(entityClass, connection)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(mapResultSetToEntity(entityClass, resultSet, connection));
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
    public <T> T insert(T entity, Connection connection) {
        Objects.requireNonNull(entity, "Entity should not be null");
        try (PreparedStatement insertStatement = prepareInsertStatement(entity, connection)) {
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
    public <T> T update(T entity, Connection connection) {
        Objects.requireNonNull(entity);
        try (PreparedStatement updateStatement = prepareUpdateStatement(entity, connection)) {
            int rowsAffected = updateStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to update entity in the database");
            return entity;
        } catch (SQLException | IllegalAccessException e) {
            throw new BibernateException(e);
        }
    }

    /**
     * Deletes an existing entity from the database.
     *
     * @param entity the entity to delete
     * @param <T>    the type of the entity
     * @return the deleted entity
     */
    public <T> T delete(T entity, Connection connection) {
        Objects.requireNonNull(entity);
        try (PreparedStatement deleteStatement = prepareDeleteStatement(entity, connection)) {
            int rowsAffected = deleteStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to delete entity from the database");
            return entity;
        } catch (SQLException e) {
            throw new BibernateException(e);
        }
    }

    private <T> PreparedStatement prepareFindStatement(Class<T> entityClass, Field field, Object fieldValue,
                                                       Connection connection) throws SQLException {
        String tableName = getTableName(entityClass);
        String columnName = getColumnName(field);
        String query = String.format(FIND_ENTITY_BY_FIELD_NAME_TEMPLATE, tableName, columnName);
        printSqlStatement(query);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setObject(1, fieldValue);
        return statement;
    }

    private <T> PreparedStatement prepareFindAllStatement(Class<T> entityClass, Connection connection) throws SQLException {
        String tableName = getTableName(entityClass);
        String query = String.format(FIND_ALL_ENTITIES_FROM_TABLE_TEMPLATE, tableName);
        printSqlStatement(query);
        return connection.prepareStatement(query);
    }

    private <T> PreparedStatement prepareDeleteStatement(T entity, Connection connection) throws SQLException {
        String tableName = getTableName(entity.getClass());
        Field idField = getIdField(entity.getClass());
        Object idValue = getIdValue(entity);
        if (idValue == null) {
            throw new BibernateException("ID field is null");
        }
        String deleteQuery = String.format(DELETE_BY_ID_TEMPLATE, tableName, getColumnName(idField));
        printSqlStatement(deleteQuery);
        PreparedStatement statement = connection.prepareStatement(deleteQuery);
        statement.setObject(1, idValue);
        return statement;
    }

    private static void throwExceptionIfRowsAffectedNotOne(int rowsAffected, String message) {
        if (rowsAffected != 1) {
            throw new BibernateException(message);
        }
    }

    private <T> PreparedStatement prepareInsertStatement(T entity, Connection connection) throws SQLException {
        String tableName = getTableName(entity.getClass());
        List<String> columns = getInsertableColumns(entity.getClass());
        List<Object> values = getInsertableValues(entity);
        String insertPlaceHolders = getInsertPlaceholders(columns);
        String insertQuery = String.format(INSERT_INTO_TABLE_VALUES_TEMPLATE, tableName,
                String.join(", ", columns), insertPlaceHolders);
        printSqlStatement(insertQuery);
        PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        setPreparedStatementValues(values, statement);
        return statement;
    }

    private <T> PreparedStatement prepareUpdateStatement(T entity, Connection connection) throws SQLException,
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
        printSqlStatement(updateQuery);
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

    private <T> T mapResultSetToEntity(Class<T> entityClass, ResultSet resultSet, Connection connection) {
        try {
            T entity = entityClass.getConstructor().newInstance();
            for (var entityField : entityClass.getDeclaredFields()) {
                entityField.setAccessible(TRUE);

                if (isRegularField(entityField)) {
                    String columnName = getColumnName(entityField);
                    var columnValue = convertToJavaType(entityField, resultSet.getObject(columnName));
                    entityField.set(entity, columnValue);
                } else if (isEntityField(entityField)) {
                    var relatedEntityClass = entityField.getType();

                    var relatedEntityId = getJoinColumnName(entityField);
                    var relatedEntityIdValue = resultSet.getObject(relatedEntityId);

                    var relatedEntity = findById(relatedEntityClass, relatedEntityIdValue, connection);
                    entityField.set(entity, relatedEntity);
                } else if (isEntityCollectionField(entityField)) {
                    var relatedEntityType = getRelatedEntityType(entityField);
                    var relatedEntityId = getIdValue(entity);
                    var ann = entityField.getAnnotation(OneToMany.class);
                    var fetchType = ann.fetchType();
                    if (fetchType.equals(FetchType.LAZY)) {
                        var relatedEntityCollection = new LazyList<T>(() -> findAll(relatedEntityType, entityField, relatedEntityId, connection));
                        entityField.set(entity, relatedEntityCollection);
                    } else {
                        var relatedEntityCollection = findAll(relatedEntityType, entityField, relatedEntityId, connection);
                        entityField.set(entity, relatedEntityCollection);
                    }

                }
            }
            return entity;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 SQLException e) {
            throw new BibernateException(e);
        }
    }

    private Class<?> getRelatedEntityType(Field entityField) {
        var paramType = (ParameterizedType) entityField.getGenericType();
        var actualTypeArgs = paramType.getActualTypeArguments();
        var actualTypeArgument = actualTypeArgs[0];

        return (Class<?>) actualTypeArgument;
    }

    private static Object convertToJavaType(Field field, Object value) throws IllegalAccessException, SQLException {
        if (value == null) {
            return null;
        }

        Class<?> fieldType = field.getType();

        if (fieldType.isAssignableFrom(value.getClass())) {
            return value;
        }

        switch (fieldType.getSimpleName()) {
            case "LocalDateTime" -> {
                if (value instanceof Timestamp) {
                    return ((Timestamp) value).toLocalDateTime();
                }
            }
            case "String" -> {
                if (value instanceof Clob clob) {
                    return clob.getSubString(1, (int) clob.length());
                }
            }
            case "LocalDate" -> {
                if (value instanceof Date) {
                    return ((Date) value).toLocalDate();
                }
            }
            case "LocalTime" -> {
                if (value instanceof Time) {
                    return ((Time) value).toLocalTime();
                }
            }
            case "Double" -> {
                if (value instanceof Float) {
                    return ((Float) value).doubleValue();
                }
            }
        }

        throw new BibernateException(String.format("Cannot convert value of type %s to field type %s",
                value.getClass().getSimpleName(), fieldType.getSimpleName()));
    }

    private void printSqlStatement(String query) {
        if (showSql) {
            System.out.println("SQL statement: " + query);
        }
    }
}
