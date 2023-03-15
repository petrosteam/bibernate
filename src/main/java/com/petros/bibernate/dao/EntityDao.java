package com.petros.bibernate.dao;

import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.util.EntityUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;

public class EntityDao {
    private final DataSource dataSource;
    private static final String FIND_ENTITY_BY_FIELD_NAME = "select * from %s where %s = ?";

    public EntityDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T findById(Class<T> entityClass, Object idValue) {
        Field idField = EntityUtil.getIdField(entityClass);
        return findOne(entityClass, idField, idValue);
    }

    public <T> T findOne(Class<T> entityClass, Field field, Object fieldValue) {
        List<T> entities = findAll(entityClass, field, fieldValue);
        if (entities.size() != 1) {
            throw new BibernateException("Result must contain exactly one row");
        }
        return entities.get(0);
    }

    public <T> List<T> findAll(Class<T> entityClass, Field field, Object fieldValue) {
        List<T> result = new ArrayList<>();

        try (var connection = dataSource.getConnection()) {
            String tableName = EntityUtil.getTableName(entityClass);
            String columnName = EntityUtil.getColumnName(field);
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

    private static <T> T mapResultSetToEntity(Class<T> entityClass, ResultSet resultSet) {
        try {
            T entity = entityClass.getConstructor().newInstance();
            for (var entityField : entityClass.getDeclaredFields()) {
                entityField.setAccessible(TRUE);
                String columnName = EntityUtil.getColumnName(entityField);
                entityField.set(entity, resultSet.getObject(columnName));
            }
            return entity;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
