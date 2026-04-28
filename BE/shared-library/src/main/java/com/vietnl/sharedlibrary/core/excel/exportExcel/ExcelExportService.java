package com.vietnl.sharedlibrary.core.excel.exportExcel;

import com.eps.shared.core.excel.exportExcel.model.ColumnConfig;
import com.eps.shared.core.excel.exportExcel.model.ExcelConfig;
import com.eps.shared.core.excel.exportExcel.model.ExcelWrapper;
import com.eps.shared.core.exception.ResponseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Interface cho dịch vụ xuất dữ liệu ra Excel với khả năng kiểm soát chi tiết và sử dụng Wrapper.
 * Cung cấp cài đặt mặc định để dễ dàng mở rộng.
 */
public interface ExcelExportService {

  int WINDOW_SIZE = 100;
  Logger log = LoggerFactory.getLogger(ExcelExportService.class);

  Map<Class<?>, BiConsumer<Cell, Object>> setCellValueCallbackMap =
      new HashMap<>() {
        {
          put(
              Number.class,
              (cell, value) -> {
                cell.setCellValue(((Number) value).doubleValue());
              });
          put(
              String.class,
              (cell, value) -> {
                cell.setCellValue(value.toString());
              });
          put(
              Boolean.class,
              (cell, value) -> {
                cell.setCellValue((Boolean) value);
              });
          put(
              Date.class,
              (cell, value) -> {
                cell.setCellValue((Date) value);
              });
          put(
              LocalDateTime.class,
              (cell, value) -> {
                cell.setCellValue((LocalDateTime) value);
              });
          put(
              LocalDate.class,
              (cell, value) -> {
                cell.setCellValue((LocalDate) value);
              });
          put(
              LocalTime.class,
              (cell, value) -> {
                cell.setCellValue(((LocalTime) value).atDate(LocalDate.of(1900, 1, 1)));
              });
        }
      };

  /**
   * Khởi tạo một ExcelWrapper mới chứa workbook và tiêu đề.
   *
   * @param config Cấu hình cho việc xuất Excel
   * @return Đối tượng ExcelWrapper đã được khởi tạo
   */
  default ExcelWrapper init(ExcelConfig config) {
    Workbook workbook = new SXSSFWorkbook(WINDOW_SIZE);
    Sheet sheet = initializeSheet(workbook, config);
    writeHeader(sheet, config);
    setupColumns(sheet, config);
    return new ExcelWrapper(workbook, sheet, config, this);
  }

  /**
   * Thêm dữ liệu vào workbook thông qua wrapper.
   *
   * @param wrapper Đối tượng bọc duy trì trạng thái xuất Excel
   * @param data Danh sách các đối tượng dữ liệu
   */
  default void addData(ExcelWrapper wrapper, Collection<?> data) {
    if (data == null || data.isEmpty()) {
      return;
    }

    Sheet sheet = wrapper.getCurrentSheet();
    ExcelConfig config = wrapper.getConfig();

    // Ghi dữ liệu và cập nhật lastRow
    int nextRow = wrapper.getLastRow() + 1;
    if (nextRow == 0) {
      nextRow = 1; // Trường hợp chưa có gì (mặc định header là 0)
    }

    int rowsAdded = writeData(sheet, data, config, nextRow);
    wrapper.setLastRow(wrapper.getLastRow() + rowsAdded);
  }

  /**
   * Chuyển đổi workbook trong wrapper thành mảng byte và giải phóng tài nguyên.
   *
   * @param wrapper Đối tượng bọc chứa workbook cần chuyển đổi
   * @return mảng byte của file Excel
   */
  default byte[] toByteArray(ExcelWrapper wrapper) {
    Workbook workbook = wrapper.getWorkbook();
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      workbook.write(bos);
      return bos.toByteArray();
    } catch (IOException e) {
      log.error("Lỗi khi chuyển đổi workbook sang mảng byte", e);
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Xuất Excel thất bại trong quá trình chuyển đổi");
    } finally {
      if (workbook instanceof SXSSFWorkbook) {
        ((SXSSFWorkbook) workbook).dispose();
      }
      try {
        workbook.close();
      } catch (IOException e) {
        log.warn("Không thể đóng workbook", e);
      }
    }
  }

  /**
   * Phương thức tiện ích xuất nhanh và trả về wrapper.
   *
   * @param data Danh sách các đối tượng dữ liệu
   * @param config Cấu hình cho việc xuất Excel
   * @return Đối tượng ExcelWrapper đã đổ đầy dữ liệu
   */
  default ExcelWrapper export(Collection<?> data, ExcelConfig config) {
    ExcelWrapper wrapper = init(config);
    addData(wrapper, data);
    return wrapper;
  }

  // --- Helper methods

  default Sheet initializeSheet(Workbook workbook, ExcelConfig config) {
    String sheetName =
        config.getSheetName() != null
            ? config.getSheetName()
            : "Sheet " + (workbook.getNumberOfSheets() + 1);
    int index = 1;
    String originalName = sheetName;
    while (workbook.getSheet(sheetName) != null) {
      sheetName = originalName + " (" + index++ + ")";
    }
    return workbook.createSheet(sheetName);
  }

  default void writeHeader(Sheet sheet, ExcelConfig config) {
    if (config.getColumns() == null || config.getColumns().isEmpty()) {
      return;
    }

    Row headerRow = sheet.createRow(0);
    CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

    int colIndex = 0;
    for (ColumnConfig col : config.getColumns()) {
      Cell cell = headerRow.createCell(colIndex++);
      cell.setCellValue(col.getHeader());
      cell.setCellStyle(headerStyle);
    }
  }

  default void setupColumns(Sheet sheet, ExcelConfig config) {
    if (config.getColumns() == null) {
      return;
    }

    int colIndex = 0;
    for (ColumnConfig col : config.getColumns()) {
      if (col.getWidth() != null) {
        sheet.setColumnWidth(colIndex, col.getWidth() * 256);
      } else {
        sheet.setColumnWidth(colIndex, 15 * 256);
      }
      colIndex++;
    }
  }

  default int writeData(Sheet sheet, Collection<?> data, ExcelConfig config, int startRowIndex) {
    int rowIndex = startRowIndex;
    Workbook workbook = sheet.getWorkbook();
    Map<String, CellStyle> styleCache = new HashMap<>();

    for (Object item : data) {
      Row row = sheet.createRow(rowIndex++);
      int colIndex = 0;
      for (ColumnConfig col : config.getColumns()) {
        Cell cell = row.createCell(colIndex++);
        Object value = getFieldValue(item, col.getFieldName());
        setCellValue(cell, value, col, workbook, styleCache);
      }
    }
    return data.size();
  }

  default Object getFieldValue(Object object, String fieldName) {
    try {
      Field field = findField(object.getClass(), fieldName);
      if (field != null) {
        field.setAccessible(true);
        return field.get(object);
      }
    } catch (Exception e) {
      log.warn("Field không hợp lệ hoặc không có quyền truy cập: {}", fieldName);
    }
    return null;
  }

  private Field findField(Class<?> clazz, String fieldName) {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  default void setCellValue(
      Cell cell,
      Object value,
      ColumnConfig col,
      Workbook workbook,
      Map<String, CellStyle> styleCache) {
    if (value == null) {
      cell.setBlank();
      return;
    }
    BiConsumer<Cell, Object> callback = setCellValueCallbackMap.get(value.getClass());
    callback.accept(cell, value);
    //        setCellValueByType(cell, value);
    applyCellFormat(cell, value, col, workbook, styleCache);
  }

  default void setCellValueByType(Cell cell, Object value) {
    if (value instanceof Number) {
      cell.setCellValue(((Number) value).doubleValue());
      return;
    }

    if (value instanceof Boolean) {
      cell.setCellValue((Boolean) value);
      return;
    }

    if (setDateTimeValue(cell, value)) {
      return;
    }

    cell.setCellValue(value.toString());
  }

  default boolean setDateTimeValue(Cell cell, Object value) {
    if (value instanceof Date) {
      cell.setCellValue((Date) value);
      return true;
    }

    if (value instanceof LocalDateTime) {
      cell.setCellValue((LocalDateTime) value);
      return true;
    }

    if (value instanceof LocalDate) {
      cell.setCellValue((LocalDate) value);
      return true;
    }

    if (value instanceof LocalTime) {
      // Excel không hỗ trợ LocalTime trực tiếp, ta chuyển về LocalDateTime với ngày gốc 1900-01-01
      cell.setCellValue(((LocalTime) value).atDate(LocalDate.of(1900, 1, 1)));
      return true;
    }

    return false;
  }

  default void applyCellFormat(
      Cell cell,
      Object value,
      ColumnConfig col,
      Workbook workbook,
      Map<String, CellStyle> styleCache) {
    if (shouldApplyFormat(value, col)) {
      CellStyle style = getCachedFormatStyle(workbook, col.getFormat(), styleCache);
      cell.setCellStyle(style);
    }
  }

  private boolean shouldApplyFormat(Object value, ColumnConfig col) {
    return col.getFormat() != null && !(value instanceof String) && !(value instanceof Boolean);
  }

  private CellStyle getCachedFormatStyle(
      Workbook workbook, String format, Map<String, CellStyle> styleCache) {
    return styleCache.computeIfAbsent(
        format, formatKey -> createFormattedStyle(workbook, formatKey));
  }

  private CellStyle createFormattedStyle(Workbook workbook, String format) {
    CellStyle style = workbook.createCellStyle();
    DataFormat dataFormat = workbook.getCreationHelper().createDataFormat();
    style.setDataFormat(dataFormat.getFormat(format));
    return style;
  }

  default CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
  }
}
