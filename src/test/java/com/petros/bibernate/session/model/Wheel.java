package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Entity;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import lombok.Data;

@Data
@Entity
@Table("wheels")
public class Wheel {
    @Id
    private Long id;

    @Column("position")
    private String position;
    @Column("side")
    private String side;
}
