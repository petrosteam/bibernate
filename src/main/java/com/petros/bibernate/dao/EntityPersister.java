package com.petros.bibernate.dao;

import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;

@Slf4j
public class EntityPersister {
    private final DataSource dataSource;
    private static final String FIND_ENTITY_BY_FIELD_NAME = "select * from %s where %s = ?;";

    public EntityPersister(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T findById(Class<T> entityClass, Object idValue) {
        Field idField = EntityUtil.getIdField(entityClass);
        log.debug("Searching for {} entity by field = {} with id = {}", entityClass.getSimpleName(), idField.getName(), idValue);
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
            String tableName = EntityUtil.getTableName(entityClass);
            String columnName = EntityUtil.getColumnName(field);
            String query = String.format(FIND_ENTITY_BY_FIELD_NAME, tableName, columnName);
            log.debug("Running query: {}", query);

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
                Object columnValue = resultSet.getObject(columnName);
                log.trace("Setting DB->Object, class = {}, field = {}, value = {}", entityClass.getSimpleName(), columnName, columnValue);
                entityField.set(entity, columnValue);
            }
            return entity;
        } catch (Exception e) {
            throw new BibernateException(e);
        }
    }
}
