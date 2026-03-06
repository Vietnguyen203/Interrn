package com.manager.account.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseListResponseDTO {
    String code = "0";
    String message = "SUCCESS";
    List<?> data;
    int pageNumber;
    int totalPage;
    long totalItem;

    public BaseListResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }
}




