package com.vietnl.sharedlibrary.core.excel.importExcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả sau khi thực hiện import Excel. Chứa cả dữ liệu thành công và danh sách lỗi chi tiết.
 *
 * @param <T> Kiểu dữ liệu của DTO.
 */
@Data
public class ImportResult<T> {

  /** Danh sách các bản ghi đã map thành công và vượt qua validation */
  @JsonIgnore private List<T> data = new ArrayList<>();

  /** Danh sách các lỗi xảy ra trong quá trình đọc và validate */
  @JsonIgnore private List<ImportError> errors = new ArrayList<>();

  private int totalRecords = 0;
  private int successRecords = 0;
  private int errorRecords = 0;
  private byte[] errorFile;

  public void addError(int rowNum, String column, String message, Object invalidValue) {
    errors.add(new ImportError(rowNum, column, message, invalidValue));
  }

  /** Xử lý đếm số dòng thành công */
  public void incrementSuccess() {
    successRecords++;
    totalRecords++;
  }

  /** Xử lý đếm số dòng lỗi */
  public void incrementError() {
    errorRecords++;
    totalRecords++;
  }

  public void addData(T item) {
    data.add(item);
  }

  /** Kiểm tra xem quá trình import có thành công hoàn toàn (không có lỗi) hay không. */
  public boolean isAllSuccess() {
    return errors.isEmpty();
  }

  /** Kiểm tra xem có dữ liệu nào được import thành công hay không. */
  public boolean hasData() {
    return !data.isEmpty();
  }
}
