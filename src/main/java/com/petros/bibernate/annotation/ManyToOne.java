package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @ManyToOne specifies an association to another entity.
 * Example:
 * <pre>{@code
 * @Entity
 * public class Car {
 *      ...
 *      @ManyToOne
 *      @JoinColumn("car_owner_id")
 *      private Person owner;
 *      ...
 * }
 * }</pre>
 *
 * @see JoinColumn
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {

    /**
     * Returns a fetchType which is Eager by default. It means that child entity will be loaded from database with
     * parent entity. Currently only EAGER behaviour is realized.
     */
    FetchType fetchType() default FetchType.EAGER;
}
