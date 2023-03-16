package com.petros.bibernate.util;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import com.petros.bibernate.exception.BibernateException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class EntityUtil {

    public static String getTableName(Class<?> entityClass) {
        return ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::value)
                .orElse(entityClass.getSimpleName());
    }

    public static String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::value)
                .orElse(field.getName());
    }

    public static Field getIdField(Class<?> entityClass) {
        List<Field> idFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .toList();
        if (idFields.size() != 1) {
            throw new BibernateException(
                    format("Entity %s must contain exactly one field annotated with @Id", entityClass.getSimpleName()));
        }
        return idFields.get(0);
    }
}
