package com.petros.bibernate.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This enum defines the priorities for various entity actions.
 * The priorities are used to order the actions during a transaction.
 */
@RequiredArgsConstructor
public enum EntityActionPriority {
    INSERT(1), UPDATE(2), DELETE(3);

    /**
     * The priority value for the entity action.
     */
    @Getter
    private final int priority;
}
