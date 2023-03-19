package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @Column specifies the name of the column in the database table that corresponds to the class field. If no annotation is present, then the field name is assumed to be column name
 * Example:
 * <pre>{@code
 * @Entity
 * public class User {
 *      ...
 *      @Column("first_name")
 *      private String firstName;
 *      ...
 * }
 * }</pre>
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * @return the name of the column
     */
    String value();
}
