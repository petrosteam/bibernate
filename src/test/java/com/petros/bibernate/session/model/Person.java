package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;

@Data
@Entity
@Table("persons")
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    @Column("first_name")
    private String firstName;
}
