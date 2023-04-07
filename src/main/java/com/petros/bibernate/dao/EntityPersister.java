package com.petros.bibernate.dao;

import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import static com.petros.bibernate.util.EntityUtil.getTableName;
import static com.petros.bibernate.util.EntityUtil.getUpdatableColumns;
import static com.petros.bibernate.util.EntityUtil.getUpdatableValues;
import static com.petros.bibernate.util.EntityUtil.isEntityField;
import static com.petros.bibernate.util.EntityUtil.isRegularField;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * This class is responsible for performing database CRUD operations on entities.
 */
@Slf4j
public class EntityPersister {
    private static final String FIND_ENTITY_BY_FIELD_NAME_TEMPLATE = "SELECT * FROM %s WHERE %s = ?;";
    private static final String FIND_ALL_ENTITIES_FROM_TABLE_TEMPLATE = "SELECT * FROM %s;";
    private static final String INSERT_INTO_TABLE_VALUES_TEMPLATE = "INSERT INTO %s(%s) VALUES (%s);";
    private static final String UPDATE_BY_ID_TEMPLATE = "UPDATE %s SET %s WHERE %s = ?;";
    private static final String DELETE_BY_ID_TEMPLATE = "DELETE FROM %s WHERE %s = ?;";
    private final boolean showSql;

    /**
     * Constructor for EntityPersister with option to show SQL statements or not.
     *
     * @param showSql flag to determine whether to show SQL statements
     */
    public EntityPersister(boolean showSql) {
        this.showSql = showSql;
    }

    /**
     * Constructor for EntityPersister without showing SQL statements.
     */
    public EntityPersister() {
        this(FALSE);
    }

    private static void throwExceptionIfRowsAffectedNotOne(int rowsAffected, String message) {
        if (rowsAffected != 1) {
            throw new BibernateException(message);
        }
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

    /**
     * Finds an entity by its ID value.
     *
     * @param entityClass the class of the entity to find
     * @param idValue     the value of the ID field
     * @param connection  the database connection to use
     * @return the entity object if it is found, otherwise null
     */
    public <T> T findById(Class<T> entityClass, Object idValue, Connection connection) {
        Field idField = getIdField(entityClass);
        return this.findOne(entityClass, idField, idValue, connection);
    }

    /**
     * Finds a single entity based on a field value.
     *
     * @param entityClass the class of the entity to find
     * @param field       the field to search by
     * @param fieldValue  the value of the field to search for
     * @param connection  the database connection to use
     * @param <T>         the entity class type
     * @return the entity object if it is found, otherwise null
     */
    public <T> T findOne(Class<T> entityClass, Field field, Object fieldValue, Connection connection) {
        log.trace("Finding entity of class {} by field {} with value: {}", entityClass.getName(), field.getName(),
                fieldValue);
        List<T> entities = findAll(entityClass, field, fieldValue, connection);
        if (entities.size() == 0) {
            log.trace("No entity found with {} = {}", field.getName(), fieldValue);
            return null;
        }
        if (entities.size() > 1) {
            throw new BibernateException("Result must contain exactly one row");
        }
        var entity = entities.get(0);
        log.trace("Found entity from {} where {} = {}: {}",
                entityClass.getSimpleName(), field.getName(), fieldValue, entity);
        return entity;
    }

    /**
     * Retrieves all entities based on a field and its value.
     *
     * @param entityClass the class of entities to be found
     * @param field       the field by which to search for entities
     * @param fieldValue  the value of the field by which to search for entities
     * @param connection  the database connection
     * @param <T>         the type of entities to be found
     * @return a list of entities found in the database
     * @throws BibernateException if an exception occurs while executing the SQL query
     */
    public <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue, Connection connection) {
        log.trace("Entering findAll() with entityClass={}, field={}, fieldValue={}, and connection={}",
                entityClass.getName(), field.getName(), fieldValue.toString(), connection.toString());
        List<T> result = new ArrayList<>();

        try (PreparedStatement statement = prepareFindStatement(entityClass, field, fieldValue, connection)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(mapResultSetToEntity(entityClass, resultSet));
            }
            log.trace("Found {} entities of type {}", result.size(), entityClass.getName());
        } catch (SQLException e) {
            log.error("Exception occurred while executing SQL query", e);
            throw new BibernateException(e);
        }
        return result;
    }

    /**
     * Retrieves all entities of specified entityClass.
     *
     * @param entityClass the class of the entity to retrieve
     * @param connection  the connection to the database
     * @param <T>         the type of the entity
     * @return a list of entities
     * @throws BibernateException if an SQLException occurs
     */
    public <T> List<T> findAll(Class<T> entityClass, Connection connection) {
        List<T> result = new ArrayList<>();
        log.trace("Retrieving all entities of class {} from database.", entityClass.getSimpleName());
        try (PreparedStatement statement = prepareFindAllStatement(entityClass, connection)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(mapResultSetToEntity(entityClass, resultSet));
            }
        } catch (SQLException e) {
            log.error("Exception occurred while executing SQL query", e);
            throw new BibernateException(e);
        }
        log.trace("Successfully retrieved {} entities of class {} from database.", result.size(),
                entityClass.getSimpleName());
        return result;
    }

    /**
     * Inserts a new entity into the database.
     *
     * @param entity     the entity to insert
     * @param connection the database connection to use
     * @param <T>        the type of the entity
     * @return the inserted entity
     * @throws BibernateException if an error occurs while inserting the entity
     */
    public <T> T insert(T entity, Connection connection) {
        Objects.requireNonNull(entity, "Entity should not be null");
        log.debug("Inserting entity {} with connection {}", entity, connection);
        try (PreparedStatement insertStatement = prepareInsertStatement(entity, connection)) {
            int rowsAffected = insertStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to insert entity into the database");
            setIdFromGeneratedKeys(entity, insertStatement);
            return entity;
        } catch (SQLException | IllegalAccessException e) {
            log.error("Exception occurred while executing SQL query", e);
            throw new BibernateException(e);
        }
    }

    /**
     * Updates an existing entity in the database.
     *
     * @param entity     the entity to update in the database
     * @param connection the connection to the database
     * @param <T>        the type of the entity to update
     * @return the updated entity
     * @throws BibernateException if there is an error updating the entity
     */
    public <T> T update(T entity, Connection connection) {
        Objects.requireNonNull(entity);
        log.trace("Updating entity of class {} in the database", entity.getClass().getSimpleName());
        try (PreparedStatement updateStatement = prepareUpdateStatement(entity, connection)) {
            int rowsAffected = updateStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to update entity in the database");
            log.trace("Entity of class {} with ID {} was updated in the database", entity.getClass().getSimpleName(),
                    getIdValue(entity));
            return entity;
        } catch (SQLException | IllegalAccessException e) {
            log.error("Exception occurred while executing SQL query", e);
            throw new BibernateException(e);
        }
    }

    /**
     * Deletes an existing entity from the database.
     *
     * @param entity     the entity to delete
     * @param connection the database connection to use
     * @return the deleted entity
     * @throws BibernateException if there was an error deleting the entity from the database
     */
    public <T> T delete(T entity, Connection connection) {
        Objects.requireNonNull(entity);
        log.trace("Deleting entity of class {} from the database", entity.getClass().getSimpleName());
        try (PreparedStatement deleteStatement = prepareDeleteStatement(entity, connection)) {
            int rowsAffected = deleteStatement.executeUpdate();
            throwExceptionIfRowsAffectedNotOne(rowsAffected, "Failed to delete entity from the database");
            log.trace("Deleted entity {} from database", entity);
            return entity;
        } catch (SQLException e) {
            log.error("Exception occurred while executing SQL query", e);
            throw new BibernateException(e);
        }
    }

    private <T> PreparedStatement prepareFindStatement(Class<T> entityClass, Field field, Object fieldValue,
                                                       Connection connection) throws SQLException {
        log.trace("Preparing find statement for entityClass={} field={} value={} connection={}",
                entityClass.getName(), field.getName(), fieldValue, connection.toString());
        String tableName = getTableName(entityClass);
        String columnName = getColumnName(field);
        String query = String.format(FIND_ENTITY_BY_FIELD_NAME_TEMPLATE, tableName, columnName);
        log.trace("Prepared find statement: {}", query);
        printSqlStatement(query);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setObject(1, fieldValue);
        return statement;
    }

    private <T> PreparedStatement prepareFindAllStatement(Class<T> entityClass, Connection connection) throws SQLException {
        log.trace("Preparing findAll statement for entityClass={}, connection={}", entityClass.getName(),
                connection.toString());
        String tableName = getTableName(entityClass);
        String query = String.format(FIND_ALL_ENTITIES_FROM_TABLE_TEMPLATE, tableName);
        log.trace("Prepared findAll statement: {}", query);
        printSqlStatement(query);
        return connection.prepareStatement(query);
    }

    private <T> PreparedStatement prepareDeleteStatement(T entity, Connection connection) throws SQLException {
        log.trace("Preparing delete statement for entityClass={}, connection={}", entity.getClass().getSimpleName(),
                connection.toString());
        String tableName = getTableName(entity.getClass());
        Field idField = getIdField(entity.getClass());
        Object idValue = getIdValue(entity);
        if (idValue == null) {
            throw new BibernateException("ID field is null");
        }
        String deleteQuery = String.format(DELETE_BY_ID_TEMPLATE, tableName, getColumnName(idField));
        log.trace("Prepared delete statement: {}", deleteQuery);
        printSqlStatement(deleteQuery);
        PreparedStatement statement = connection.prepareStatement(deleteQuery);
        statement.setObject(1, idValue);
        return statement;
    }

    private <T> PreparedStatement prepareInsertStatement(T entity, Connection connection) throws SQLException {
        log.trace("Preparing insert statement for entityClass={}, connection={}", entity.getClass().getSimpleName(),
                connection.toString());
        String tableName = getTableName(entity.getClass());
        List<String> columns = getInsertableColumns(entity.getClass());
        List<Object> values = getInsertableValues(entity);
        String insertPlaceHolders = getInsertPlaceholders(columns);
        String insertQuery = String.format(INSERT_INTO_TABLE_VALUES_TEMPLATE, tableName,
                String.join(", ", columns), insertPlaceHolders);
        log.trace("Prepared insert statement: {}", insertQuery);
        printSqlStatement(insertQuery);
        PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        setPreparedStatementValues(values, statement);
        return statement;
    }

    private <T> PreparedStatement prepareUpdateStatement(T entity, Connection connection) throws SQLException,
            IllegalAccessException {
        log.trace("Preparing update statement for entityClass={}, connection={}", entity.getClass().getSimpleName(),
                connection.toString());
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
        log.trace("Prepared update statement: {}", updateQuery);
        printSqlStatement(updateQuery);
        PreparedStatement statement = connection.prepareStatement(updateQuery);
        setPreparedStatementValues(updateValues, statement);
        statement.setObject(updateValues.size() + 1, idValue);
        return statement;
    }

    private <T> T mapResultSetToEntity(Class<T> entityClass, ResultSet resultSet) {
        log.trace("Creating entity {} from the result set", entityClass.getSimpleName());
        try {
            T entity = entityClass.getConstructor().newInstance();
            for (var entityField : entityClass.getDeclaredFields()) {
                entityField.setAccessible(TRUE);

                if (isRegularField(entityField)) {
                    String columnName = getColumnName(entityField);
                    var columnValue = convertToJavaType(entityField, resultSet.getObject(columnName));
                    entityField.set(entity, columnValue);
                    log.trace("Setting field '{}' with value '{}' for entity of class {}",
                            columnName, columnValue, entityClass.getSimpleName());
                } else if (isEntityField(entityField)) {
                    var relatedEntityClass = entityField.getType();

                    var relatedEntityId = getColumnName(entityField);
                    var relatedEntityIdValue = resultSet.getObject(relatedEntityId);
                    log.trace("Setting related entity field '{}' with ID value '{}' for entity of class {}",
                            relatedEntityId, relatedEntityIdValue, entityClass.getSimpleName());
                    Object o = relatedEntityClass.getConstructor().newInstance();
                    Field declaredField = EntityUtil.getIdField(relatedEntityClass);
                    declaredField.setAccessible(TRUE);
                    declaredField.set(o, relatedEntityIdValue);

                    entityField.set(entity, o);
                }
            }
            return entity;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 SQLException e) {
            log.error("Exception occurred while mapping result set to entity of class {}",
                    entityClass.getSimpleName(), e);
            throw new BibernateException(e);
        }
    }

    private void printSqlStatement(String query) {
        if (showSql) {
            System.out.println("SQL statement: " + query);
        }
    }
}
