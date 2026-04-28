package com.vietnl.sharedlibrary.core.excel.importExcel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.eps.shared.core.excel.importExcel.model.ImportResult;
import com.eps.shared.core.validation.ValidatorUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class ExcelImportListener<T> implements ReadListener<T> {

  /** Mặc định xử lý mỗi lần 100 dòng */
  private static final int DEFAULT_BATCH_COUNT = 100;

  private final int batchCount;
  private final ImportResult<T> importResult;
  private final Consumer<List<T>> batchConsumer;
  private final List<T> cachedDataList;
  private final Validator validator;
  private final Consumer<T> validatorConsumer;

  /**
   * Khởi tạo Listener.
   *
   * @param importResult Đối tượng chứa kết quả trả về.
   * @param batchSize Số dòng xử lý trong mỗi đợt.
   * @param batchConsumer Hàm callback dùng để xử lý dữ liệu sau mỗi đợt (ví dụ: save database).
   */
  public ExcelImportListener(
      ImportResult<T> importResult,
      int batchSize,
      Consumer<T> validatorConsumer,
      Consumer<List<T>> batchConsumer) {
    this.importResult = importResult;
    batchCount = batchSize > 0 ? batchSize : DEFAULT_BATCH_COUNT;
    this.batchConsumer = batchConsumer;
    cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
    validator = ValidatorUtils.getValidator();
    this.validatorConsumer = validatorConsumer;
  }

  public ExcelImportListener(ImportResult<T> importResult) {
    this(importResult, DEFAULT_BATCH_COUNT, null, null);
  }

  @Override
  public void invoke(T data, AnalysisContext context) {
    int currentRow = context.readRowHolder().getRowIndex() + 1;

    boolean hasError = runAnnotationValidation(data, currentRow);
    if (runCustomValidation(data, currentRow, hasError)) {
      hasError = true;
    }

    if (!hasError) {
      collectValidData(data);
    }

    flushBatchIfFull();
  }

  /** Thực hiện validate annotation (@NotBlank, @Size, ...), ghi lỗi vào importResult. */
  private boolean runAnnotationValidation(T data, int currentRow) {
    Set<ConstraintViolation<T>> violations = validator.validate(data);
    if (violations.isEmpty()) {
      return false;
    }
    for (ConstraintViolation<T> violation : violations) {
      importResult.addError(
          currentRow,
          violation.getPropertyPath().toString(),
          violation.getMessage(),
          violation.getInvalidValue());
    }
    importResult.incrementError();
    return true;
  }

  /** Thực hiện custom validation qua validatorConsumer, ghi lỗi nếu có exception. */
  private boolean runCustomValidation(T data, int currentRow, boolean hasError) {
    if (validatorConsumer == null) {
      return false;
    }
    try {
      validatorConsumer.accept(data);
      return false;
    } catch (Exception e) {
      importResult.addError(currentRow, null, e.getMessage(), null);
      if (!hasError) {
        importResult.incrementError();
      }
      return true;
    }
  }

  /** Thêm dòng hợp lệ vào cache và kết quả. */
  private void collectValidData(T data) {
    cachedDataList.add(data);
    importResult.addData(data);
    importResult.incrementSuccess();
  }

  /** Kích hoạt xử lý batch khi cache đủ số lượng. */
  private void flushBatchIfFull() {
    if (cachedDataList.size() >= batchCount) {
      processData();
      cachedDataList.clear();
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    // Xử lý nốt những dữ liệu còn lại trong cache
    if (!cachedDataList.isEmpty()) {
      processData();
    }
  }

  /** Xử lý lỗi định dạng file/kiểu dữ liệu */
  @Override
  public void onException(Exception exception, AnalysisContext context) {
    int currentRow = context.readRowHolder().getRowIndex() + 1;
    importResult.addError(
        currentRow, null, "Lỗi định dạng dữ liệu: " + exception.getMessage(), null);
    importResult.incrementError();
  }

  /** Xử lý lưu dữ liệu batch */
  private void processData() {
    if (batchConsumer != null && !cachedDataList.isEmpty()) {
      batchConsumer.accept(cachedDataList);
    }
  }
}
