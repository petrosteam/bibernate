package com.petros.bibernate.util.model;

import com.petros.bibernate.annotation.Id;

public class User {
    @Id
    private Long id;

    @Id
    private Long anotherId;
}
