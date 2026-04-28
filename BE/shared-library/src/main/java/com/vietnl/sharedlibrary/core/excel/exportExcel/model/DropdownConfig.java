package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Cấu hình cho một dropdown (Data Validation) trên một cột cụ thể. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropdownConfig {

  /** Tên header cột hiển thị dropdown cho người dùng chọn */
  private String headerName;

  /**
   * Tên header của cột ẩn chứa giá trị thực tế (ID). Nếu có, code sẽ tự động thêm công thức VLOOKUP
   * để map Name -> ID.
   */
  private String value;

  /** Danh sách các options (chứa cả nhãn và giá trị) */
  private List<DropdownOption> options;

  /** Legacy support: Danh sách các chuỗi đơn thuần. Nếu dùng cái này thì label = value. */
  @Deprecated private List<String> oldOptions;
}
