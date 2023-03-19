package com.petros.bibernate.util;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import com.petros.bibernate.exception.BibernateException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class EntityUtil {

    /**
     * Retrieves the database table name from a given entity type. Returns the value of annotation @Table if exists, otherwise simple class name is returned
     * @param entityClass entity class that is mapped to database table
     * @return the table name
     */
    public static String getTableName(Class<?> entityClass) {
        return ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::value)
                .orElse(entityClass.getSimpleName());
    }

    /**
     * Retrieves the database column name from a given class field. Returns the value of annotation @Column if exists, otherwise simple field name is returned
     * @param field field that is mapped to database column
     * @return the column name
     */
    public static String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::value)
                .orElse(field.getName());
    }

    /**
     * Retrieves the field annotated with @Id from a given entity class.
     * @param entityClass entity class that is mapped to database table
     * @return the field marked with @Id
     */
    public static Field getIdField(Class<?> entityClass) {
        List<Field> idFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(EntityUtil::isIdField)
                .toList();
        if (idFields.size() != 1) {
            throw new BibernateException(
                    format("Entity %s must contain exactly one field annotated with @Id", entityClass.getSimpleName()));
        }
        return idFields.get(0);
    }

    public static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    public static List<String> getInsertableColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isIdField(field))
                .map(EntityUtil::getColumnName)
                .collect(Collectors.toList());
    }

    public static List<Object> getInsertableValues(Object entity)  {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> !isIdField(field))
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    try {
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new BibernateException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
