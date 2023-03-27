package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;

@Data
@Entity
@Table("notes")
public class Note {
    @Id
    private Long id;

    @Column("body")
    private String body;

    @ManyToOne
    @JoinColumn("person_id")
    private Person person;
}
