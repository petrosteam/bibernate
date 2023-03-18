package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @Entity defines the classes managed by Bibernate. Every class that is mapped to database table must be annotated with @Entity and contain exactly one field annotated with @Id
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
}
