package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table("cars")
public class Car {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @OneToMany(mappedBy = "car")
    private List<Wheel> wheels = new ArrayList<>();
}
