package com.vietnl.sharedlibrary.core.excel.exportExcel.resolver;

import com.eps.shared.core.excel.exportExcel.annotation.ExcelDropdown;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import java.util.Arrays;
import java.util.List;

/** Resolver lấy dữ liệu từ Enum Class. */
public class EnumResolver implements DropdownOptionResolver {

  @Override
  public boolean supports(ExcelDropdown annotation, ExcelConfig config) {
    return annotation.enumClass() != ExcelDropdown.DefaultEnum.class;
  }

  @Override
  public List<DropdownOption> resolve(ExcelDropdown annotation, ExcelConfig config) {
    //    boolean needValue = !annotation.value().isEmpty();
    return Arrays.stream(annotation.enumClass().getEnumConstants())
        .map(e -> new DropdownOption(e.name(), e.name()))
        .toList();
  }
}
