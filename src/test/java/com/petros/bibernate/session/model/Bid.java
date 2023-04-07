package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table("bids")
@EqualsAndHashCode(of = "id")
public class Bid {
    @Id
    private Long id;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn("item_id")
    private Item item;
}
