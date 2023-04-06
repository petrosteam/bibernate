package com.petros.bibernate.session.model;

import com.petros.bibernate.annotation.Column;
import com.petros.bibernate.annotation.Entity;
import com.petros.bibernate.annotation.GeneratedValue;
import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table("products")
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    @Column("name")
    private String productName;

    private String producer;

    private BigDecimal price;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("is_available")
    private Boolean isAvailable;

    @Column("stock_count")
    private Integer stockCount;

    @Column("weight")
    private Double weight;

    @Column("description")
    private String description;

    @Column("sale_date")
    private LocalDate saleDate;

    @Column("sale_time")
    private LocalTime saleTime;
}
