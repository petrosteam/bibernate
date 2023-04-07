package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table("eager_wheel_cars")
public class EagerWheelCar {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @OneToMany(fetchType = FetchType.EAGER, mappedBy = "car")
    private List<Wheel> wheels = new ArrayList<>();
}
