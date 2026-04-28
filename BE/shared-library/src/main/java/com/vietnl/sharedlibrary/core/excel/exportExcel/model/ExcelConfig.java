package com.vietnl.sharedlibrary.core.excel.exportExcel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelConfig {
  /** Tên của sheet trong file Excel */
  private String sheetName;

  /** Danh sách cấu hình các cột */
  private List<ColumnConfig> columns;

  private List<DropdownConfig> dropdownConfigs;

  /** Data nguồn cho dropdown (Key trùng với data() trong @ExcelDropdown) */
  private Map<String, List<DropdownOption>> dropdowns = new HashMap<>();
}
