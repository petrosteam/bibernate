package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Entity;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table("products")
public class Product {
    @Id
    private Long id;

    @Column("name")
    private String productName;

    @Column("producer")
    private String producer;

    @Column("price")
    private BigDecimal price;
}
