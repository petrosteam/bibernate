package com.petros.bibernate.annotation;

/**
 * FetchType is a parameter used to set a strategy for relations in {@link OneToMany} and {@link ManyToOne} annotations.
 */
public enum FetchType {
    /**
     * LAZY strategy means that related entities won't be fetched from the database until they are required.
     */
    LAZY,

    /**
     * EAGER strategy means that related entities will be fetched from the database eagerly.
     */
    EAGER
}
