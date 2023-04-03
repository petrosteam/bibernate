package com.petros.bibernate.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EntityActionPriority {
    INSERT(1), UPDATE(2), DELETE(3);

    @Getter
    private final int priority;

}
