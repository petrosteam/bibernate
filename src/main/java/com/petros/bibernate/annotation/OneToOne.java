package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a single-valued association to another entity that has one-to-one relation.
 *
 * Example:
 * <pre>{@code
 * @Entity
 * public class Owner {
 *      ...
 *      @OneToOne
 *      private OwnerDetails ownerDetails;
 *      ...
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
}
