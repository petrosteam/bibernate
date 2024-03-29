package com.petros.bibernate.util;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.GeneratedValue;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.JoinColumn;
import com.petros.bibernate.annotation.ManyToOne;
import com.petros.bibernate.annotation.MapsId;
import com.petros.bibernate.annotation.OneToMany;
import com.petros.bibernate.annotation.OneToOne;
import com.petros.bibernate.annotation.Table;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.context.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * A utility class for working with database entities.
 */
@Slf4j
public class EntityUtil {

    /**
     * Retrieves the database table name from a given entity type. Returns the value of annotation @Table if exists,
     * otherwise simple class name is returned
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
     * Retrieves the database column name from a given class field. Returns the value of annotation @Column if
     * exists, otherwise simple field name is returned
     *
     * @param field field that is mapped to database column
     * @return the column name
     */
    public static String getColumnName(Field field) {
        if (EntityUtil.isEntityField(field) || EntityUtil.isEntityCollectionField(field)) {
            return ofNullable(field.getAnnotation(JoinColumn.class))
                    .map(JoinColumn::value)
                    .orElse(getDefaultIdColumnName(field.getName()));
        } else {
            return ofNullable(field.getAnnotation(Column.class))
                    .map(Column::value)
                    .orElse(field.getName());
        }
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
                .orElse(getDefaultIdColumnName(field.getName()));
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
     */
    public static Object getIdValue(Object entity) {
        var entityClass = entity.getClass();
        var idField = getIdField(entityClass);
        idField.setAccessible(true);
        try {
            return idField.get(entity);
        } catch (IllegalAccessException e) {
            log.error("Could not retrieve ID from entity {}", entityClass.getSimpleName(), e);
            throw new BibernateException("Could not retrieve ID from entity " + entity.getClass().getSimpleName(), e);
        }
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
     * Determines whether a field is annotated with {@link Id} and {@link GeneratedValue}.
     *
     * @param field the field to check
     * @return true if the field is annotated with {@link Id} and {@link GeneratedValue}, false otherwise
     */
    public static boolean isGeneratedIdField(Field field) {
        return field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(GeneratedValue.class);
    }

    /**
     * Retrieves the list of columns that are insertable for a given entity class.
     * This method excludes columns annotated with {@link Id} and {@link GeneratedValue} and fields marked with
     * {@link MapsId}.
     *
     * @param entityClass the entity class for which to retrieve the insertable columns
     * @return the list of insertable columns
     */
    public static List<String> getInsertableColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isGeneratedIdField(field))
                .filter(field -> !field.isAnnotationPresent(MapsId.class))
                .map(EntityUtil::getColumnName)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of entity values for a given entity.
     * Fields marked with {@link OneToMany}, {@link ManyToOne}, {@link OneToOne} annotations are ignored
     *
     * @param entity Bibernate entity
     * @return the list of entity values
     */
    public static List<Object> getEntityFieldsForSnapshot(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> !isEntityRelationField(field))
                .map(field -> getFieldValue(field, entity))
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
                    f.setAccessible(true);
                    return getFieldValue(f, entity);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of values that are insertable for a given entity.
     * Values corresponding to fields annotated with {@link Id} are excluded from the list.
     * If a field is annotated with {@link MapsId}, the ID value is taken from the corresponding nested entity.
     *
     * @param entity the entity for which to retrieve the insertable values
     * @return the list of insertable values
     */
    public static List<Object> getInsertableValues(Object entity) {
        Field mapsIdField = getMapsIdField(entity.getClass());
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> !isGeneratedIdField(field))
                .filter(field -> !field.isAnnotationPresent(MapsId.class))
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    if (isIdField(field) && mapsIdField != null) {
                        mapsIdField.setAccessible(true);
                        return getIdValueFromNestedEntity(entity, mapsIdField);
                    } else if (EntityUtil.isEntityField(field)) {
                        return getIdValueFromNestedEntity(entity, field);
                    } else {
                        return getFieldValue(field, entity);
                    }
                })
                .collect(Collectors.toList());
    }

    private static Object getIdValueFromNestedEntity(Object entity, Field field) {
        var nestedEntity = getFieldValue(field, entity);
        return nestedEntity == null ? null : EntityUtil.getIdValue(nestedEntity);
    }

    /**
     * Retrieves the field marked with {@link MapsId} from a given entity class.
     *
     * @param entityClass the entity class for which to retrieve the field marked with {@link MapsId}
     * @return the field marked with {@link MapsId}, or null if no such field exists
     * @throws BibernateException if there are multiple fields marked with {@link MapsId}
     */
    public static Field getMapsIdField(Class<?> entityClass) {
        List<Field> mapsIdFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(EntityUtil::isEntityField)
                .filter(f -> f.isAnnotationPresent(MapsId.class))
                .toList();
        if (mapsIdFields.size() > 1) {
            log.error("Entity {} has multiple fields annotated with @MapsId", entityClass.getSimpleName());
            throw new BibernateException(format(
                    "Entity %s has multiple fields annotated with @MapsId", entityClass.getSimpleName()));
        }
        return mapsIdFields.isEmpty() ? null : mapsIdFields.get(0);
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
     * Checks if value is annotated with one of annotations
     * that means complicated entity relations
     *
     * @param field the field to check
     * @return true if field has simple value and not related to another entity
     */
    public static boolean isRegularField(Field field) {
        return !isEntityField(field) && !isEntityCollectionField(field);
    }

    /**
     * Helper method that returns entity fields that not marked with @OneToMany annotation.
     *
     * @param entityType type of Bibernate entity
     * @return array of fields that are suitable for dirty checking
     */
    public static Field[] getEntityColumns(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !isEntityRelationField(field))
                .toArray(Field[]::new);
    }

    public static Field[] getEntityRelationFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(EntityUtil::isEntityRelationField)
                .toArray(Field[]::new);
    }

    /**
     * Getting field value of entity
     *
     * @param field  object field
     * @param entity Bibernate entity
     * @return field value
     */
    public static Object getFieldValue(Field field, Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.error("Could not get field {} value for entity {}", field.getName(), getTableName(entity.getClass()));
            throw new BibernateException("Could not get field %s value for entity %s".formatted(field.getName(),
                    getTableName(entity.getClass())));
        }
    }

    /**
     * Checks if value is annotated with {@link ManyToOne} or  {@link OneToOne} annotation
     * that means complicated entity relations
     *
     * @param field the field to check
     * @return true if entity has ManyToOne relation
     */
    public static boolean isEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    public static boolean isEntityCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    private static String getDefaultIdColumnName(String fieldName) {
        return fieldName + "_id";
    }

    /**
     * Checking if entity field is marked with {@link OneToMany} {@link OneToOne} or {@link ManyToOne} annotation.
     * If field is marked with these annotations, it could not be added to snapshot context
     *
     * @param field entity field
     * @return true if field has one of annotations described above
     * @see PersistenceContext#getSnapshotDiff()
     */
    public static boolean isEntityRelationField(Field field) {
        return field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class);
    }

    public static Class<?> getRelatedEntityType(Field entityField) {
        var paramType = (ParameterizedType) entityField.getGenericType();
        var actualTypeArgs = paramType.getActualTypeArguments();
        var actualTypeArgument = actualTypeArgs[0];

        return (Class<?>) actualTypeArgument;
    }
}
