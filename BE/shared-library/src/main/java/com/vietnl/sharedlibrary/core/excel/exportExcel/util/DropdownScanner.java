package com.vietnl.sharedlibrary.core.excel.exportExcel.util;

import com.eps.shared.core.excel.exportExcel.annotation.ExcelDropdown;
import com.eps.shared.core.excel.exportExcel.model.DropdownConfig;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import com.eps.shared.core.excel.exportExcel.resolver.ConfigMapResolver;
import com.eps.shared.core.excel.exportExcel.resolver.DropdownOptionResolver;
import com.eps.shared.core.excel.exportExcel.resolver.EnumResolver;
import com.eps.shared.core.excel.exportExcel.resolver.FixedOptionsResolver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility quét các annotation {@link ExcelDropdown} trên class và trả về cấu hình dropdown. */
public class DropdownScanner {

  private static final List<DropdownOptionResolver> RESOLVERS =
      List.of(new ConfigMapResolver(), new EnumResolver(), new FixedOptionsResolver());

  private DropdownScanner() {}

  /**
   * Thu thập tất cả dropdown configs từ ExcelConfig + scan annotation trên các class.
   *
   * @param config Cấu hình excel (có thể chứa dropdownConfigs sẵn)
   * @param itemClasses Các class chứa @ExcelDropdown
   * @return Danh sách tổng hợp dropdown configs
   */
  public static List<DropdownConfig> scanAll(ExcelConfig config, Class<?>... itemClasses) {
    List<DropdownConfig> allConfigs = new ArrayList<>();

    if (config != null && config.getDropdownConfigs() != null) {
      allConfigs.addAll(config.getDropdownConfigs());
    }

    if (itemClasses != null) {
      for (Class<?> clazz : itemClasses) {
        allConfigs.addAll(scan(clazz, config));
      }
    }

    return allConfigs;
  }

  /**
   * Quét class của items để tìm các cấu hình dropdown.
   *
   * @param clazz Class của đối tượng dữ liệu
   * @param excelConfig Cấu hình excel chứa map data linh động
   * @return Danh sách cấu hình dropdown
   */
  public static List<DropdownConfig> scan(Class<?> clazz, ExcelConfig excelConfig) {
    List<DropdownConfig> configs = new ArrayList<>();

    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(ExcelDropdown.class)) {
        ExcelDropdown annotation = field.getAnnotation(ExcelDropdown.class);
        List<DropdownOption> options = resolveOptions(annotation, excelConfig);

        if (!options.isEmpty()) {
          String valueHeader =
              annotation.value().isEmpty() ? annotation.headerName() : annotation.value();
          configs.add(
              DropdownConfig.builder()
                  .headerName(annotation.headerName())
                  .value(valueHeader)
                  .options(options)
                  .build());
        }
      }
    }

    return configs;
  }

  private static List<DropdownOption> resolveOptions(ExcelDropdown annotation, ExcelConfig config) {
    for (DropdownOptionResolver resolver : RESOLVERS) {
      if (resolver.supports(annotation, config)) {
        return resolver.resolve(annotation, config);
      }
    }
    return Collections.emptyList();
  }
}
