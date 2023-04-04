package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;

@Data
@Entity
@Table("person_info")
public class PersonInfo {
    @Id
    private Long id;

    @Column("info")
    String info;

    @OneToOne
    @JoinColumn("id")
    private Person person;
}
