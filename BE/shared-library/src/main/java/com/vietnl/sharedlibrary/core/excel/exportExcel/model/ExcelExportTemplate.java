package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import com.eps.shared.core.excel.exportExcel.ExcelTemplateExportUtil;
import com.eps.shared.core.exception.ResponseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.*;

@Getter
public class ExcelExportTemplate {

  public static final String DATA_KEY = "data";

  private final ExcelConfig config;

  /** Trả về live reference — ExcelTemplateExportUtil dùng để thêm/xóa entries. */
  private final List<DropdownConfig> allDropdowns;

  /** Trả về live reference — ExcelTemplateExportUtil dùng để put dữ liệu. */
  private final Map<String, Object> data;

  /** Bản gốc template dùng cho JXLS streaming. */
  private final byte[] originalTemplateBytes;

  public ExcelExportTemplate(InputStream templateIs, ExcelConfig config) {
    this.config = config;
    allDropdowns = new ArrayList<>();
    data = new HashMap<>();

    try {
      originalTemplateBytes = templateIs.readAllBytes();
    } catch (Exception e) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi đọc template: " + e.getMessage());
    }
  }

  /**
   * Ghi danh sách dữ liệu vào template với key mặc định "data".
   *
   * @param items danh sách dữ liệu cần xuất
   * @return this (fluent API)
   */
  public ExcelExportTemplate addData(Collection<?> items) {
    return ExcelTemplateExportUtil.addData(this, items);
  }

  /**
   * Ghi dữ liệu với key tùy chỉnh vào template.
   *
   * @param key tên biến trong template
   * @param value giá trị
   * @return this (fluent API)
   */
  public ExcelExportTemplate addData(String key, Object value) {
    data.put(key, value);
    return this;
  }

  /**
   * Xuất ra byte[] kết quả (fill data qua JXLS).
   *
   * @return byte[] nội dung file Excel hoàn chỉnh
   */
  public byte[] toByteArray() {
    return ExcelTemplateExportUtil.toByteArray(this);
  }
}
