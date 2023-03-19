package com.petros.bibernate.dao;

import com.petros.bibernate.exception.BibernateException;

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
import static com.petros.bibernate.util.EntityUtil.getInsertableColumns;
import static com.petros.bibernate.util.EntityUtil.getInsertableValues;
import static com.petros.bibernate.util.EntityUtil.getTableName;
import static java.lang.Boolean.TRUE;

public class EntityPersister {
    private final DataSource dataSource;
    private static final String FIND_ENTITY_BY_FIELD_NAME = "select * from %s where %s = ?;";
    private static final String INSERT_INTO_TABLE_VALUES = "insert into %s(%s) values (%s);";

    public EntityPersister(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T findById(Class<T> entityClass, Object idValue) {
        Field idField = getIdField(entityClass);
        return findOne(entityClass, idField, idValue);
    }

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

    public <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue) {
        List<T> result = new ArrayList<>();

        try (var connection = dataSource.getConnection()) {
            String tableName = getTableName(entityClass);
            String columnName = getColumnName(field);
            String query = String.format(FIND_ENTITY_BY_FIELD_NAME, tableName, columnName);

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

    public <T> T insert(T entity) {
        Objects.requireNonNull(entity);
        try (var connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = prepareInsertStatement(entity, connection);

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected != 1) {
                throw new BibernateException("Failed to insert entity into the database");
            }
            setIdFromGeneratedKeys(entity, insertStatement);
            return entity;

        } catch (SQLException | IllegalAccessException e) {
            throw new BibernateException(e);
        }
    }

    private static <T> PreparedStatement prepareInsertStatement(T entity, Connection connection) throws SQLException {
        String tableName = getTableName(entity.getClass());
        List<String> columns = getInsertableColumns(entity.getClass());
        List<Object> values = getInsertableValues(entity);
        String insertPlaceHolders = getInsertPlaceholders(columns);
        String insertQuery = String.format(INSERT_INTO_TABLE_VALUES, tableName, String.join(", ", columns),
                insertPlaceHolders);

        PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        setPreparedStatementValues(values, statement);
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

    private static String getInsertPlaceholders(List<?> insertableValues) {
        return insertableValues.stream()
                .map(f -> "?")
                .collect(Collectors.joining(","));
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
