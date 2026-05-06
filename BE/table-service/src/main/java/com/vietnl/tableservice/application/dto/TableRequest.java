package com.vietnl.tableservice.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TableRequest {
    private Integer tableNumber;
    private Integer capacity;
    private String location;
    private UUID areaRefId;
}