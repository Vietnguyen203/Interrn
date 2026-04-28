package com.vietnl.sharedlibrary.core.excel.exportExcel.resolver;

import com.eps.shared.core.excel.exportExcel.annotation.ExcelDropdown;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import java.util.List;

/** Interface cho các chiến lược giải quyết nguồn dữ liệu Dropdown. */
public interface DropdownOptionResolver {

  /** Kiểm tra xem resolver này có hỗ trợ cấu hình hiện tại không. */
  boolean supports(ExcelDropdown annotation, ExcelConfig config);

  /** Thực hiện lấy danh sách options từ nguồn dữ liệu. */
  List<DropdownOption> resolve(ExcelDropdown annotation, ExcelConfig config);
}
