package com.manager.account.domain.models.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@javax.persistence.Table(name = "RESTAURANT_TABLES")
public class Table extends Base {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Column(name = "SERVER")
    private String server;

    @Column(name = "CHAIRS")
    private Integer chairs;

    @Column(name = "CURRENT_ORDER_ID")
    private String currentOrderId;
}
