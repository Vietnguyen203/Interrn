package com.vietnl.sharedlibrary.core.exception;

public enum CommonErrorMessage implements BaseErrorMessage {
  FORBIDDEN("Bạn không có quyền truy cập"),
  VALIDATION_FAILED("Xác minh dữ liệu thất bại"),
  FIELD_CANT_SORT("Trường #fieldname# không thể sắp xếp"),
  INTERNAL_SERVER("Hệ thống có lỗi xảy ra xin vui lòng thử lại sau"),
  ENUM_FAILED("Không thể chuyển đổi #value# thành loại #type#"),
  MISSING_PARAMETER("Thiếu tham số bắt buộc"),
  NOT_FOUND("Không tìm thấy dữ liệu"),
  INVALID_PARAMETER("Giá trị '#value#' không hợp lệ cho tham số '#parameter#'"),
  FEIGN_ERROR("Giao tiếp service qua Feign gặp sự cố"),
  INIT_RESOURCE_FEATURE_ERROR("Khởi tạo resource feature bị lỗi"),
  NO_RESOURCE_NOT_FOUND("Đường dẫn truy cập tài nguyên không tồn tại"),
  MISSING_ANNOTATION("Annotation @RolesAllowed không được cấu hình đúng"),
  UNAUTHORIZED_MISSING_HEADER("Thiếu header X-User trong request"),
  REQUEST_CONTEXT_NOT_FOUND("Không thể lấy thông tin request hiện tại"),
  FROM_TABLE_IS_REQUIRED("From table is required"),
  OBJECT_NOT_FOUND("Đối tượng không tồn tại"),
  DATABASE_NOT_CONNECTED("Database chưa được kết nối"),
  INVALID_HEADER_FORMAT("Định dạng X-User header không hợp lệ"),
  INVALID_JSON("#string# không phải chuỗi JSON");

  CommonErrorMessage(String message) {
    val = message;
  }

  private final String val;

  @Override
  public String val() {
    return val;
  }
}
