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
 *      @OneToMany(mappedBy = "car")
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

    /**
     * Defines a bidirectional relationship between two entities.
     * The mappedBy attribute is used on the inverse side of the relationship to specify the owning side.
     * It indicates that the relationship is bidirectional and that the entity on this side is not responsible for the association column(s).
     *
     * @return field name of related entity
     */
    String mappedBy();

    FetchType fetchType() default FetchType.LAZY;
}
