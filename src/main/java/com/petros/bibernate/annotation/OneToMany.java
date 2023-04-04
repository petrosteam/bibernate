package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @OneToMany defines one-to-many entity associations. This annotation applies to the field that represents
 * collection of elements. Generic type of collection determines related entity type.
 *
 * Example:
 * <pre>{@code
 * @Entity
 * public class Owner {
 *      ...
 *      @OneToMany
 *      private List<Car> cars;
 *      ...
 * }
 * }</pre>
 * @see ManyToOne
 *
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
}
