package com.vietnl.sharedlibrary.core.excel.importExcel;

import com.alibaba.excel.EasyExcel;
import com.eps.shared.core.excel.importExcel.listener.ExcelImportListener;
import com.eps.shared.core.excel.importExcel.model.ImportError;
import com.eps.shared.core.excel.importExcel.model.ImportResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface ExcelImportUtil {

  /** Số dòng header mặc định — dòng 9 là header, data bắt đầu từ dòng 10 */
  int DEFAULT_HEAD_ROW_NUMBER = 9;

  static <T> ImportResult<T> importExcel(
      MultipartFile file, Class<T> dtoClass, Consumer<T> validator, Consumer<List<T>> batchConsumer)
      throws Exception {
    return importExcel(file, dtoClass, 100, DEFAULT_HEAD_ROW_NUMBER, validator, batchConsumer);
  }

  /**
   * Import file Excel có cấu hình Batch Size và hàm xử lý lô dữ liệu.
   *
   * @param <T> Kiểu DTO.
   * @param file File Excel.
   * @param dtoClass Class DTO.
   * @param batchSize Số lượng dòng xử lý mỗi đợt.
   * @param batchConsumer Hàm xử lý (ví dụ save database theo lô).
   */
  static <T> ImportResult<T> importExcel(
      MultipartFile file,
      Class<T> dtoClass,
      int batchSize,
      Consumer<T> validator,
      Consumer<List<T>> batchConsumer)
      throws Exception {
    return importExcel(
        file, dtoClass, batchSize, DEFAULT_HEAD_ROW_NUMBER, validator, batchConsumer);
  }

  /**
   * Import file Excel với đầy đủ tùy chọn cấu hình.
   *
   * @param <T> Kiểu DTO.
   * @param file File Excel.
   * @param dtoClass Class DTO.
   * @param batchSize Số lượng dòng xử lý mỗi đợt.
   * @param headRowNumber Số dòng header (dòng data bắt đầu từ headRowNumber + 1).
   * @param validator Hàm validate nghiệp vụ từng dòng.
   * @param batchConsumer Hàm xử lý (ví dụ save database theo lô).
   */
  static <T> ImportResult<T> importExcel(
      MultipartFile file,
      Class<T> dtoClass,
      int batchSize,
      int headRowNumber,
      Consumer<T> validator,
      Consumer<List<T>> batchConsumer)
      throws Exception {
    byte[] fileBytes = file.getBytes();
    try (InputStream is = new ByteArrayInputStream(fileBytes)) {
      ImportResult<T> result = new ImportResult<>();
      EasyExcel.read(
              is, dtoClass, new ExcelImportListener<>(result, batchSize, validator, batchConsumer))
          .sheet()
          .headRowNumber(headRowNumber)
          .doRead();

      if (!result.isAllSuccess()) {
        result.setErrorFile(generateErrorFile(fileBytes, result, headRowNumber));
      }
      return result;
    }
  }

  /**
   * Tạo tệp nhật ký lỗi định dạng Excel (byte array) dựa trên đối tượng ImportResult. Tất cả dòng
   * hợp lệ sẽ bị xóa, chỉ các dòng gặp lỗi mới được giữ lại và ghi thêm cột lỗi.
   *
   * @param fileBytes Mảng byte của tệp Excel ban đầu
   * @param result Đối tượng lưu trữ ImportResult chứa danh sách lỗi (ImportError)
   * @param headerRowCount Số thứ tự (row index) của dòng tiêu đề
   * @return Mảng byte của tệp Excel chứa các lỗi
   * @throws Exception Các ngoại lệ xảy ra trong quá trình thao tác trên File / Workbook
   */
  static <T> byte[] generateErrorFile(byte[] fileBytes, ImportResult<T> result, int headerRowCount)
      throws Exception {
    Map<Integer, String> errorMap = extractErrorMap(result);

    try (InputStream is = new ByteArrayInputStream(fileBytes);
        Workbook workbook = WorkbookFactory.create(is);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.getSheetAt(0);
      int errorColIndex = appendErrorHeaderAndGetColIndex(sheet, headerRowCount);
      processErrorRows(sheet, errorMap, headerRowCount, errorColIndex);

      workbook.write(bos);
      return bos.toByteArray();
    }
  }

  /**
   * Trích xuất danh sách lỗi thành Map. Mục đích để tra cứu nhanh lỗi theo dòng. Chuyển RowNum
   * (1-based) thành RowIndex (0-based) dùng cho Apache POI. Nếu có nhiều lỗi trên 1 dòng, chúng sẽ
   * đc cộng dồn bằng dấu \n.
   *
   * @param result ImportResult có chứa các lỗi
   * @return Bản đồ ánh xạ: [Chỉ số Dòng (0-based) -> Chuỗi thông báo lỗi tổng hợp]
   */
  static <T> Map<Integer, String> extractErrorMap(ImportResult<T> result) {
    return result.getErrors().stream()
        .collect(
            Collectors.toMap(
                e -> e.getRowNum() - 1,
                ImportError::getMessage,
                (msg1, msg2) -> msg1 + ", \n" + msg2));
  }

  /**
   * Gắn thêm cột "Ghi chú lỗi" vào dòng tiêu đề của Excel. Giữ lại style của cột kề trước nếu có
   * nhằm đồng bộ giao diện.
   *
   * @param sheet Bảng tính Excel đang thao tác
   * @param headerRowCount Số thứ tự (1-based) của dòng tiêu đề
   * @return Số thứ tự (Index) của cột lỗi vừa tạo
   */
  static int appendErrorHeaderAndGetColIndex(Sheet sheet, int headerRowCount) {
    int headerRowIndex = Math.max(0, headerRowCount - 1);
    Row headerRow = sheet.getRow(headerRowIndex);

    if (headerRow == null) {
      return 0;
    }

    int errorColIndex = Math.max(0, headerRow.getLastCellNum());
    Cell errorHeaderCell = headerRow.createCell(errorColIndex);
    errorHeaderCell.setCellValue("Ghi chú lỗi");

    Cell prevCell = headerRow.getCell(errorColIndex - 1);
    if (prevCell != null && prevCell.getCellStyle() != null) {
      errorHeaderCell.setCellStyle(prevCell.getCellStyle());
    }

    return errorColIndex;
  }

  /**
   * Duyệt qua dữ liệu (tính từ sau dòng tiêu đề tới cuối file). Lấy lỗi từ errorMap điền vào cột
   * lỗi, nếu dòng đó không có lỗi -> xóa bỏ. Đồng thời setWrapText(true) để ô lỗi tự động xuống
   * dòng nếu có ký tự \n.
   *
   * @param sheet Bảng tính Excel đang thao tác
   * @param errorMap Cặp ChỉSốDòng - ChúThíchLỗi
   * @param headerRowCount Số thứ tự của header row limit bắt đầu xử lý data
   * @param errorColIndex Cột để ghi dữ liệu lỗi (0-based columnIndex)
   */
  static void processErrorRows(
      Sheet sheet, Map<Integer, String> errorMap, int headerRowCount, int errorColIndex) {

    Workbook workbook = sheet.getWorkbook();
    CellStyle wrapStyle = workbook.createCellStyle();
    wrapStyle.setWrapText(true);

    for (int i = headerRowCount; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }

      String errorMsg = errorMap.get(i);
      if (errorMsg != null) {
        Cell cell = row.createCell(errorColIndex);
        cell.setCellValue(errorMsg);
        // cell.setCellStyle(wrapStyle);
      } else {
        sheet.removeRow(row);
      }
    }
  }
}
