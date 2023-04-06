package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Entity;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.JoinColumn;
import com.petros.bibernate.annotation.OneToMany;
import com.petros.bibernate.annotation.Table;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table("cars")
public class Car {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @OneToMany
    @JoinColumn("car_id")
    private List<Wheel> wheels;
}
