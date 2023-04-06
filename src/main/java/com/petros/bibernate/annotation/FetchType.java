package com.petros.bibernate.annotation;

/**
 * FetchType is a parameter for to set a strategy for relations in {@link OneToMany} and {@link ManyToOne}.
 * LAZY strategy means that related entities won't be fetched from database until they required.
 * EAGER strategy means that related entities will be fetched from database eagerly.
 */
public enum FetchType {
    LAZY,
    EAGER
}
