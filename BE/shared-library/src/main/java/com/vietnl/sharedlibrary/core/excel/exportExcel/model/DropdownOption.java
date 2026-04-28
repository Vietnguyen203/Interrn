package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Đại diện cho một lựa chọn trong dropdown bao gồm nhãn hiển thị và giá trị thực tế. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropdownOption {
  /** Nhãn hiển thị cho người dùng (ví dụ: Tên ứng dụng) */
  private String label;

  /** Giá trị thực tế (ví dụ: ID ứng dụng) */
  private Object value;
}
