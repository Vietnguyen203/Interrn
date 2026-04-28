package com.vietnl.sharedlibrary.core.excel.exportExcel.util;

import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Lớp hỗ trợ tải cấu hình Excel từ tệp JSON trong tài nguyên (resources). */
@Component
public class ExcelConfigLoader {

  private final ObjectMapper objectMapper;

  public ExcelConfigLoader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Tải cấu hình từ đường dẫn chỉ định.
   *
   * @param path Đường dẫn đến tệp JSON trong classpath (ví dụ: "excel-config.json")
   * @return Đối tượng ExcelConfig
   * @throws IOException Nếu không thể đọc tệp
   */
  public ExcelConfig loadConfig(String path) throws IOException {
    ClassPathResource resource = new ClassPathResource(path);
    return objectMapper.readValue(resource.getInputStream(), ExcelConfig.class);
  }
}
