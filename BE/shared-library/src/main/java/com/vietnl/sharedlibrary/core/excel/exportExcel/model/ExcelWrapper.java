package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import com.eps.shared.core.excel.exportExcel.ExcelExportService;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collection;

/** Lớp bọc Workbook để duy trì trạng thái và hỗ trợ thêm dữ liệu lặp lại. */
@Getter
@Setter
public class ExcelWrapper {
  private Workbook workbook;
  private Sheet currentSheet;
  private int lastRow;
  private ExcelConfig config;
  private ExcelExportService service;

  public ExcelWrapper(
      Workbook workbook, Sheet currentSheet, ExcelConfig config, ExcelExportService service) {
    this.workbook = workbook;
    this.currentSheet = currentSheet;
    this.config = config;
    this.service = service;
    lastRow = currentSheet.getLastRowNum();
  }

  /**
   * Thêm dữ liệu vào workbook hiện tại.
   *
   * @param data Danh sách dữ liệu cần thêm
   * @return chính đối tượng wrapper này để hỗ trợ gọi chuỗi (fluent API)
   */
  public ExcelWrapper addData(Collection<?> data) {
    service.addData(this, data);
    return this;
  }

  /**
   * Chuyển đổi workbook thành mảng byte để trả về qua API.
   *
   * @return mảng byte của file Excel
   */
  public byte[] toByteArray() {
    return service.toByteArray(this);
  }
}
