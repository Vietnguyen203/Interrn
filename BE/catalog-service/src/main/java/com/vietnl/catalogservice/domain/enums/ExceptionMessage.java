package com.vietnl.catalogservice.domain.enums;

public enum ExceptionMessage {
    MISSING_REQUIRED_FIELD("Thiếu trường thông tin bắt buộc: %s"),
    CATEGORY_NOT_FOUND("Danh mục không tồn tại"),
    ITEM_NOT_FOUND("Món ăn không tồn tại"),
    CATEGORY_CODE_DUPLICATE("Mã danh mục đã tồn tại"),
    ITEM_CODE_DUPLICATE("Mã món ăn đã tồn tại"),
    INVALID_PRICE("Giá phải lớn hơn 0");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
