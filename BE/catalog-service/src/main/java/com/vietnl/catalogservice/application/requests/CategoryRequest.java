package com.vietnl.catalogservice.application.requests;

import lombok.Data;

@Data
public class CategoryRequest {
    private String code;
    private String name;
    private String description;
}
