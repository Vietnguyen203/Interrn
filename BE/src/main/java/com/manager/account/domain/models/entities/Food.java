package com.manager.account.domain.models.entities;

import com.manager.account.domain.models.enums.FoodCategory;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@javax.persistence.Table(name = "FOOD")
public class Food {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "FOOD_NAME")
    private String foodName;

    @Column(name = "IMAGE")
    private String image;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "UNIT")
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY")
    private FoodCategory category;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_AT")
    private Long createdAt;

    @Column(name = "SERVER")
    private String server;
}
