package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Entity;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import lombok.Data;

@Data
@Entity
@Table("persons")
public class Person {
    @Id
    private Long id;

    @Column("first_name")
    private String firstName;
}
