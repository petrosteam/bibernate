package com.petros.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation @JoinColumn specifies the name of the foreign key column.
 * If @JoinColumn is used with {@link ManyToOne} annotation - it means that foreign keyin the table of the source entity.
 * <p>
 * Example:
 * <pre>{@code
 * @Entity
 *  * public class Car {
 *  *      ...
 *  *      @ManyToOne
 *  *      @JoinColumn("car_owner_id")
 *  *      private Person owner;
 *  *      ...
 *  * }
 *  * }</pre>
 *
 * @see ManyToOne
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
    String value();
}
