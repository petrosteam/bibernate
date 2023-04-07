package com.petros.bibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that the entity identifier is mapped by the currently annotated @OneToOne association.
 *
 * <p>The {@code @MapsId} annotation can be used to indicate that the identifier of the owning entity in a
 * {@code @OneToOne} association is the same as the identifier of the associated entity. This is useful when
 * you want to use the identifier of the associated entity as the primary key of the owning entity, instead
 * of generating a separate identifier for the owning entity.</p>
 */
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapsId {
}
