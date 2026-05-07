package com.vietnl.notificationservice.domain.models.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CommonErrorMessage implements BaseErrorMessage {
    FORBIDDEN("Bạn không có quyền truy cập"),
    VALIDATION_FAILED("Xác minh dữ liệu thất bại"),
    NOT_FOUND("Không tìm thấy dữ liệu"),
    INTERNAL_SERVER("Hệ thống có lỗi xảy ra xin vui lòng thử lại sau"),
    ENUM_FAILED("Không thể chuyển đổi giá trị thành loại tương ứng"),
    FIELD_CANT_SORT("Trường dữ liệu này không thể sắp xếp");

    private final String val;

    @Override
    public String val() {
        return val;
    }
}
