package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @Id specifies the field of the entity class that corresponds to the primary key column in the database.
 * Every class marked with annotation @Entity must contain exactly one field marked with @Id.
 * <p>
 * Example:
 * <pre>{@code
 *      @Entity
 *      public class User {
 *          @Id
 *          private Long id;
 *          ...
 *      }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}
