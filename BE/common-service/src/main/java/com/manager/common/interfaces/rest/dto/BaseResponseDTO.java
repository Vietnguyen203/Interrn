package com.manager.common.interfaces.rest.dto;

import lombok.Data;

@Data
public class BaseResponseDTO {
    private String code;
    private String message;
    private Object data;
    private PageMeta page;

    public BaseResponseDTO() {}

    public BaseResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponseDTO(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponseDTO(String code, String message, Object data, PageMeta page) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.page = page;
    }
}
