package com.petros.bibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The GeneratedValue annotation may be applied to a primary key property or field of an entity or mapped superclass
 * in conjunction with the Id annotation.
 * The use of the GeneratedValue annotation is only required to be supported for simple primary keys.
 * This annotation indicates that the ID is generated automatically, and does not need to be inserted manually.
 *
 * @see Id
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface GeneratedValue {
}
