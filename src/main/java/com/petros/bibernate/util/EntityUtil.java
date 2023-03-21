package com.petros.bibernate.util;

import com.petros.bibernate.annotation.*;
import com.petros.bibernate.exception.BibernateException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * A utility class for working with database entities.
 */
public class EntityUtil {

    /**
     * Retrieves the database table name from a given entity type. Returns the value of annotation @Table if exists, otherwise simple class name is returned
     *
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
     *
     * @param field field that is mapped to database column
     * @return the column name
     */
    public static String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::value)
                .orElse(field.getName());
    }

    /**
     * Retrieves the JoinColumn name from a given class field.
     *
     * @param field field that is mapped as a foreign key
     * @return the value of annotation @JoinColumn
     */
    public static String getJoinColumnName(Field field) {
        return ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::value)
                .orElse(field.getName());
    }

    /**
     * Retrieves the field annotated with @Id from a given entity class.
     *
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

    /**
     * Retrieves the value of the field annotated with {@link Id} from a given entity.
     *
     * @param entity the entity from which to retrieve the {@link Id} field value
     * @return the value of the field marked with {@link Id}
     * @throws IllegalAccessException if the field is inaccessible
     */
    public static Object getIdValue(Object entity) throws IllegalAccessException {
        var entityClass = entity.getClass();
        var idField = getIdField(entityClass);
        idField.setAccessible(true);
        return idField.get(entity);
    }

    /**
     * Determines whether a field is annotated with {@link Id}.
     *
     * @param field the field to check
     * @return true if the field is annotated with {@link Id}, false otherwise
     */
    public static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    /**
     * Retrieves the list of columns that are insertable for a given entity class.
     * Columns annotated with {@link Id} are excluded from the list.
     *
     * @param entityClass the entity class for which to retrieve the insertable columns
     * @return the list of insertable columns
     */
    public static List<String> getInsertableColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isIdField(field))
                .map(EntityUtil::getColumnName)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of insertable values for a given entity.
     * Values corresponding to fields annotated with {@link Id} are excluded from the list.
     *
     * @param entity the entity for which to retrieve the insertable values
     * @return the list of insertable values
     */
    public static List<Object> getInsertableValues(Object entity) {
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

    /**
     * Retrieves the list of updatable columns for a given entity.
     * Columns corresponding to fields annotated with {@link Id} are excluded from the list.
     *
     * @param entity the entity for which to retrieve the updatable columns
     * @return the list of updatable columns
     */
    public static <T> List<String> getUpdatableColumns(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> !isIdField(field))
                .map(EntityUtil::getColumnName)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of updatable values for a given entity.
     * Values corresponding to fields annotated with {@link Id} are excluded from the list.
     *
     * @param entity the entity for which to retrieve the updatable values
     * @return the list of updatable values
     */
    public static <T> List<Object> getUpdatableValues(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> !isIdField(field))
                .map(f -> {
                    try {
                        f.setAccessible(true);
                        return f.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new BibernateException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if field has a representation in relation database.
     *
     * @param field
     * @return true if field has relation to DB entity
     */
    public static boolean hasEntityRelation(Field field) {
        return Arrays.stream(field.getAnnotations())
                .anyMatch(a -> a.annotationType().isAssignableFrom(Column.class)
                        || a.annotationType().isAssignableFrom(Id.class));
    }

    /**
     * Checks if value is annotated with one of annotations
     * that means complicated entity relations
     *
     * @param field the field to check
     * @return true if field has simple value and not related to another entity
     */
    public static boolean isRegularField(Field field) {
        return !isEntityField(field);
    }

    /**
     * Checks if value is annotated with {@link ManyToOne} annotation
     * that means complicated entity relations
     *
     * @param field the field to check
     * @return true if entity has ManyToOne relation
     */
    public static boolean isEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

}
