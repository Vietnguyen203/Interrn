package com.vietnl.catalogservice.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", length = 255)
    private String code;

    @Column(name = "food_name", length = 255)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status")
    private Integer status;
}
