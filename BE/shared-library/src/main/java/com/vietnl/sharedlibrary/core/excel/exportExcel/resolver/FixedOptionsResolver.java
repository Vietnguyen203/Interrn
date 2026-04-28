package com.vietnl.sharedlibrary.core.excel.exportExcel.resolver;

import com.eps.shared.core.excel.exportExcel.annotation.ExcelDropdown;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import java.util.Arrays;
import java.util.List;

/** Resolver lấy dữ liệu từ mảng options cố định trên annotation. */
public class FixedOptionsResolver implements DropdownOptionResolver {

  @Override
  public boolean supports(ExcelDropdown annotation, ExcelConfig config) {
    return annotation.options().length > 0;
  }

  @Override
  public List<DropdownOption> resolve(ExcelDropdown annotation, ExcelConfig config) {
    boolean needValue = !annotation.value().isEmpty();
    return Arrays.stream(annotation.options())
        .map(s -> new DropdownOption(s, needValue ? s : null))
        .toList();
  }
}
