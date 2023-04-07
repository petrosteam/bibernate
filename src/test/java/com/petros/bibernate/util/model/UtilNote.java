package com.petros.bibernate.util.model;

import com.petros.bibernate.annotation.*;

@Table("notes")
public class UtilNote {
    @Id
    @GeneratedValue
    private Long id;

    @Column("body")
    private String body;

    @ManyToOne
    @JoinColumn("person_id")
    private Person person;
}
