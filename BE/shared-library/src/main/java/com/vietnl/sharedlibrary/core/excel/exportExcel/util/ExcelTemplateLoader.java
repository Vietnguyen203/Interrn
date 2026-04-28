package com.vietnl.sharedlibrary.core.excel.exportExcel.util;

import com.eps.shared.core.exception.ResponseException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.InputStream;

/** Lớp hỗ trợ tải file template Excel (.xlsx) từ classpath. */
public class ExcelTemplateLoader {

  private ExcelTemplateLoader() {}

  /**
   * Tải file template từ classpath.
   *
   * @param templatePath Đường dẫn đến file template trong classpath (ví dụ:
   *     "templates/excel/report.xlsx")
   * @return InputStream của file template
   */
  public static InputStream loadTemplate(String templatePath) {
    try {
      ClassPathResource resource = new ClassPathResource(templatePath);
      return resource.getInputStream();
    } catch (Exception e) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy template: " + templatePath);
    }
  }
}
