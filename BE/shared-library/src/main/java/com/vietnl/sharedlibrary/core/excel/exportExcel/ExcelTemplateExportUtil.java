package com.vietnl.sharedlibrary.core.excel.exportExcel;

import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import com.eps.shared.core.excel.exportExcel.model.ExcelExportTemplate;
import com.eps.shared.core.excel.exportExcel.util.DropdownHelper;
import com.eps.shared.core.excel.exportExcel.util.DropdownScanner;
import com.eps.shared.core.excel.exportExcel.util.ExcelTemplateLoader;
import com.eps.shared.core.exception.ResponseException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

public interface ExcelTemplateExportUtil {

  /** Xuất Excel từ đường dẫn classpath . */
  static byte[] export(
      String templatePath, Collection<?> items, ExcelConfig config, Class<?>... itemClasses) {
    return init(templatePath, config, itemClasses).addData(items).toByteArray();
  }

  /** Xuất Excel từ InputStream . */
  static byte[] export(
      InputStream templateIs, Collection<?> items, ExcelConfig config, Class<?>... itemClasses) {
    return init(templateIs, config, itemClasses).addData(items).toByteArray();
  }

  /** Xuất template trống (có dropdown, không fill data) từ đường dẫn classpath. */
  static byte[] exportTemplate(String templatePath, ExcelConfig config, Class<?>... itemClasses) {
    return init(templatePath, config, itemClasses).toByteArray();
  }

  /** Xuất template trống (có dropdown, không fill data) từ InputStream. */
  static byte[] exportTemplate(
      InputStream templateIs, ExcelConfig config, Class<?>... itemClasses) {
    return init(templateIs, config, itemClasses).toByteArray();
  }

  /**
   * Khởi tạo ExcelTemplate từ đường dẫn classpath + setup dropdown từ các class.
   *
   * @param templatePath đường dẫn template trong classpath
   * @param config cấu hình excel
   * @param itemClasses các class chứa @ExcelDropdown để scan và ghi dropdown vào workbook
   * @return ExcelTemplate sẵn sàng addData và toByteArray
   */
  static ExcelExportTemplate init(
      String templatePath, ExcelConfig config, Class<?>... itemClasses) {
    if (templatePath == null || templatePath.isEmpty()) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Đường dẫn template không được để trống");
    }
    InputStream is = ExcelTemplateLoader.loadTemplate(templatePath);
    ExcelExportTemplate t = new ExcelExportTemplate(is, config);
    t.getAllDropdowns().addAll(DropdownScanner.scanAll(config, itemClasses));
    return t;
  }

  /**
   * Khởi tạo ExcelTemplate từ InputStream + setup dropdown từ các class.
   *
   * @param templateIs InputStream của file template
   * @param config cấu hình excel
   * @param itemClasses các class chứa @ExcelDropdown để scan và ghi dropdown vào workbook
   * @return ExcelTemplate sẵn sàng addData và toByteArray
   */
  static ExcelExportTemplate init(
      InputStream templateIs, ExcelConfig config, Class<?>... itemClasses) {
    if (templateIs == null) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Template InputStream không được null");
    }
    ExcelExportTemplate t = new ExcelExportTemplate(templateIs, config);
    t.getAllDropdowns().addAll(DropdownScanner.scanAll(config, itemClasses));
    return t;
  }

  /** Fill dữ liệu vào template */
  static ExcelExportTemplate addData(ExcelExportTemplate t, Collection<?> items) {
    t.getData().put(ExcelExportTemplate.DATA_KEY, items);
    return t;
  }

  /** Xuất file Excel có data. */
  static byte[] toByteArray(ExcelExportTemplate t) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try (InputStream is = new ByteArrayInputStream(t.getOriginalTemplateBytes())) {
        JxlsPoiTemplateFillerBuilder.newInstance()
            .withTemplate(is)
            .withStreaming(JxlsStreaming.STREAMING_ON)
            .build()
            .fill(t.getData(), () -> os);
      }

      try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(os.toByteArray()));
          ByteArrayOutputStream finalOs = new ByteArrayOutputStream()) {
        if (!t.getAllDropdowns().isEmpty()) {
          Sheet dataSheet = workbook.getSheetAt(0);
          DropdownHelper.applyDropdowns(workbook, dataSheet, t.getAllDropdowns());
        }
        workbook.setForceFormulaRecalculation(true);
        workbook.write(finalOs);
        return finalOs.toByteArray();
      }

    } catch (Exception e) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi tạo file Excel: " + e.getMessage());
    }
  }
}
