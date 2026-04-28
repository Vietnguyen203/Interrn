package com.vietnl.sharedlibrary.core.excel.importExcel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Đại diện cho một lỗi xảy ra tại một dòng/cột cụ thể trong file Excel. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
  /** Số dòng (1-indexed) */
  private int rowNum;

  /** Tên cột hoặc vị trí cột */
  private String column;

  /** Nội dung lỗi */
  private String message;

  /** Giá trị gây lỗi (nếu có) */
  private Object invalidValue;
}
