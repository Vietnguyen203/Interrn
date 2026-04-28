package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnConfig {
  /** Tiêu đề hiển thị của cột */
  private String header;

  /** Tên trường dữ liệu trong đối tượng Java */
  private String fieldName;

  /** Độ rộng của cột (tùy chọn) */
  private Integer width = 15;

  /** Định dạng dữ liệu (tùy chọn, ví dụ: "dd/MM/yyyy" hoặc "#,##0.00") */
  private String format;
}
