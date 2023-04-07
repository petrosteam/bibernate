package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a single-valued association to another entity that has one-to-one relation.
 * We use {@link JoinColumn} annotation to specify foreign key in current entity that points to parent entity.
 * <br>
 * Example:
 * <pre>{@code
 * @Entity
 * public class OwnerDetails {
 *      ...
 *      @OneToOne
 *      @JoinColumn("owner_id")
 *      private Owner owner;
 *      ...
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
}
