package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table("items")
@ToString(exclude = "bids")
@EqualsAndHashCode(of = "id")
public class Item {
    @Id
    private Long id;

    private String name;

    @OneToMany(fetchType = FetchType.EAGER, mappedBy = "item")
    private List<Bid> bids = new ArrayList<>();
}
