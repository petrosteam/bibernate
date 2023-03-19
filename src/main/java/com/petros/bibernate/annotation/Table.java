package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @Table specifies the name of the table in the database that corresponds to the class. If no annotation is present, then the class name is assumed to be table name
 * Example:
 * <pre>{@code
 * @Entity
 * @Table("users")
 * public class User {
 *      ...
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * @return the name of the table
     */
    String value();
}
