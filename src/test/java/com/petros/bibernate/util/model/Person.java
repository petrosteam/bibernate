package com.petros.bibernate.util.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.GeneratedValue;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;

import java.math.BigDecimal;

@Table("persons")
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    @Column("first_name")
    private String firstName;

    @Column("price")
    private BigDecimal price;
}
