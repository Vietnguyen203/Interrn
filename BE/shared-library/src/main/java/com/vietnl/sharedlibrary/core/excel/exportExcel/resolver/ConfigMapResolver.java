package com.vietnl.sharedlibrary.core.excel.exportExcel.resolver;

import com.eps.shared.core.excel.exportExcel.annotation.ExcelDropdown;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import java.util.List;

/** Resolver lấy dữ liệu từ Map cấu hình trong ExcelConfig. */
public class ConfigMapResolver implements DropdownOptionResolver {

  @Override
  public boolean supports(ExcelDropdown annotation, ExcelConfig config) {
    if (config == null) {
      return false;
    }
    String key = annotation.data().isEmpty() ? annotation.headerName() : annotation.data();
    return config.getDropdowns() != null && config.getDropdowns().containsKey(key);
  }

  @Override
  public List<DropdownOption> resolve(ExcelDropdown annotation, ExcelConfig config) {
    String key = annotation.data().isEmpty() ? annotation.headerName() : annotation.data();
    return config.getDropdowns().get(key);
  }
}
