package com.petros.bibernate.util.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;

import java.math.BigDecimal;

@Table("persons")
public class Person {
    @Id
    private Long id;

    @Column("first_name")
    private String firstName;

    private BigDecimal price;
}
