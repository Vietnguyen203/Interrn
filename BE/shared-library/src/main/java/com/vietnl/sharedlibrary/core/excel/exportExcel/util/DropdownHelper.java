package com.vietnl.sharedlibrary.core.excel.exportExcel.util;

import com.eps.shared.core.excel.exportExcel.model.DropdownConfig;
import com.eps.shared.core.excel.exportExcel.model.DropdownOption;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DropdownHelper {

  /** Tiền tố cho vùng đặt tên danh sách hiển thị (Label) */
  private static final String NAMED_RANGE_PREFIX = "Drop_";

  /** Tiền tố cho vùng đặt tên bản đồ ánh xạ (Label -> Value) */
  private static final String MAPPING_RANGE_PREFIX = "Map_";

  /** Pattern các ký tự không hợp lệ trong tên Sheet của Excel */
  private static final String SHEET_NAME_INVALID_CHARS_PATTERN = "[\\\\/?*\\[\\]]";

  /** Công thức tham chiếu đến vùng danh sách nhãn (Column A) */
  private static final String LABEL_RANGE_FORMULA = "'%s'!$A$1:$A$%d";

  /** Công thức tham chiếu đến vùng dữ liệu ánh xạ (Column A:B) */
  private static final String MAPPING_RANGE_FORMULA = "'%s'!$A$1:$B$%d";

  /**
   * Công thức VLOOKUP để tìm ID dựa trên Label đã chọn: =IFERROR(VLOOKUP(Label, Table, 2, FALSE),
   * "")
   */
  private static final String VLOOKUP_FORMULA = "IFERROR(VLOOKUP(%s, %s, 2, FALSE), \"\")";

  /** Số dòng buffer thêm cho dropdown (để user thêm data sau export). */
  private static final int DROPDOWN_ROW_BUFFER = 1000;

  private DropdownHelper() {}

  /**
   * Áp dụng đầy đủ danh sách dropdown (setup + post-process). Dùng khi không tách pipeline.
   *
   * @param workbook Workbook cần xử lý
   * @param dataSheet Sheet chứa dữ liệu chính
   * @param dropdowns Danh sách cấu hình dropdown
   */
  public static void applyDropdowns(
      Workbook workbook, Sheet dataSheet, List<DropdownConfig> dropdowns) {
    setupDropdowns(workbook, dataSheet, dropdowns, 0);
    postProcessDropdowns(dataSheet, dropdowns);
  }

  /**
   * Tạo option sheets và named ranges. Ghi cấu hình dropdown vào template trước khi nạp dữ liệu.
   * Data validation được gắn sau trong postProcess để tương thích với việc chèn cột.
   *
   * @param workbook Workbook cần xử lý
   * @param dataSheet Sheet chứa dữ liệu chính
   * @param dropdowns Danh sách cấu hình dropdown
   * @param startIndex Chỉ số bắt đầu cho đặt tên (Drop_X, Map_X)
   */
  public static void setupDropdowns(
      Workbook workbook, Sheet dataSheet, List<DropdownConfig> dropdowns, int startIndex) {
    if (dropdowns == null || dropdowns.isEmpty()) {
      return;
    }
    for (int i = 0; i < dropdowns.size(); i++) {
      setupDropdownConfig(workbook, dataSheet, dropdowns.get(i), startIndex + i);
    }
  }

  /**
   * Dịch ngược giá trị (ID → Label) và thiết lập VLOOKUP. Cần gọi sau khi JXLS đã điền dữ liệu thực
   * tế vào template.
   *
   * @param dataSheet Sheet chứa dữ liệu chính
   * @param dropdowns Danh sách cấu hình dropdown (phải cùng thứ tự với setupDropdowns)
   */
  public static void postProcessDropdowns(Sheet dataSheet, List<DropdownConfig> dropdowns) {
    if (dropdowns == null || dropdowns.isEmpty()) {
      return;
    }
    for (int i = 0; i < dropdowns.size(); i++) {
      postProcessDropdownConfig(dataSheet, dropdowns.get(i), i);
    }
  }

  /** Tạo option sheet và named ranges (trước fill). Không gắn data validation ở đây. */
  private static void setupDropdownConfig(
      Workbook workbook, Sheet dataSheet, DropdownConfig config, int index) {
    if (config.getOptions() == null || config.getOptions().isEmpty()) {
      return;
    }

    Sheet optionSheet = prepareOptionSheet(workbook, config);

    boolean isMappingValue = config.getValue() != null && !config.getValue().isEmpty();
    setupNamedRanges(workbook, optionSheet, index, config.getOptions().size(), isMappingValue);
  }

  /**
   * Translate values, chèn cột ID, gắn VLOOKUP và data validation (sau fill). Thứ tự:
   * translateValues → insertColumn → VLOOKUP → dataValidation.
   */
  private static void postProcessDropdownConfig(Sheet dataSheet, DropdownConfig config, int index) {
    if (config.getOptions() == null || config.getOptions().isEmpty()) {
      return;
    }

    int[] labelPos = findHeaderPosition(dataSheet, config.getHeaderName());
    if (labelPos == null) {
      return;
    }

    int lastDataRow = dataSheet.getLastRowNum();
    translateValuesToLabels(dataSheet, config, labelPos, lastDataRow);

    boolean isMappingValue = config.getValue() != null && !config.getValue().isEmpty();
    if (isMappingValue) {
      applyMapping(dataSheet, config, index, labelPos, lastDataRow);
    }

    applyDropdown(dataSheet, index, labelPos, lastDataRow);
  }

  /**
   * Duyệt qua cột dữ liệu và chuyển đổi các giá trị value (vừa được JXLS điền) sang Nhãn (Label)
   * hiển thị
   */
  private static void translateValuesToLabels(
      Sheet sheet, DropdownConfig config, int[] labelPos, int lastDataRow) {
    if (config.getOptions() == null || config.getOptions().isEmpty()) {
      return;
    }

    // map value -> label
    Map<String, String> valueToLabelMap =
        config.getOptions().stream()
            .filter(opt -> opt.getValue() != null)
            .collect(
                Collectors.toMap(
                    opt -> opt.getValue().toString(),
                    DropdownOption::getLabel,
                    (existing, replacement) -> existing));

    if (valueToLabelMap.isEmpty()) {
      return;
    }

    // Quét cột dữ liệu để ghi đè Label
    int startRow = labelPos[0] + 1;
    int colIdx = labelPos[1];

    for (int r = startRow; r <= lastDataRow; r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        continue;
      }

      Cell cell = row.getCell(colIdx);
      if (cell == null) {
        continue;
      }

      // Lấy giá trị hiện tại và tìm nhãn tương ứng
      String rawValue = cell.toString().trim();
      if (valueToLabelMap.containsKey(rawValue)) {
        String label = valueToLabelMap.get(rawValue);
        cell.setBlank();
        cell.setCellValue(label);
      }
    }
  }

  private static Sheet prepareOptionSheet(Workbook workbook, DropdownConfig config) {
    Sheet optionSheet = createOptionSheet(workbook, config.getHeaderName());
    writeOptions(optionSheet, config.getOptions());
    workbook.setSheetHidden(workbook.getSheetIndex(optionSheet), true);
    return optionSheet;
  }

  private static void setupNamedRanges(
      Workbook workbook, Sheet optionSheet, int index, int optionCount, boolean isMappingValue) {
    String labelRangeName = NAMED_RANGE_PREFIX + index;
    String mappingRangeName = isMappingValue ? MAPPING_RANGE_PREFIX + index : null;
    createNamedRanges(
        workbook, optionSheet, labelRangeName, mappingRangeName, optionCount, isMappingValue);
  }

  private static void applyDropdown(Sheet dataSheet, int index, int[] labelPos, int lastDataRow) {
    String labelRangeName = NAMED_RANGE_PREFIX + index;
    applyDataValidation(dataSheet, labelRangeName, labelPos[1], labelPos[0], lastDataRow);
  }

  private static void applyMapping(
      Sheet dataSheet, DropdownConfig config, int index, int[] labelPos, int lastDataRow) {
    // Tạo cột value
    int[] idPos = createValueColumn(dataSheet, config.getValue(), labelPos);

    String mappingRangeName = MAPPING_RANGE_PREFIX + index;
    applyVLookupFormula(dataSheet, mappingRangeName, labelPos[1], idPos[1], idPos[0], lastDataRow);
    dataSheet.setColumnHidden(idPos[1], true); // ẩn cột
  }

  /**
   * thêm mới cột value ngay trước cột label
   *
   * @param sheet
   * @param idHeaderName
   * @param labelPos
   * @return
   */
  private static int[] createValueColumn(Sheet sheet, String idHeaderName, int[] labelPos) {
    int headerRowIdx = labelPos[0];
    int labelCol = labelPos[1];
    Row headerRow = sheet.getRow(headerRowIdx);

    // Lấy Style và giá trị của cột Label hiện tại trước khi shift
    Cell originalCell = headerRow.getCell(labelCol);
    CellStyle headerStyle = (originalCell != null) ? originalCell.getCellStyle() : null;

    int lastCol = findLastColumn(sheet);

    // Dịch chuyển các cột sang phải
    if (lastCol >= labelCol) {
      sheet.shiftColumns(labelCol, lastCol, 1);
    }

    // load lại headerRow sau shiftColumns
    headerRow = sheet.getRow(headerRowIdx);

    // Tạo ô Header cho cột ID mới tại vị trí labelCol
    Cell idHeaderCell = headerRow.createCell(labelCol);
    idHeaderCell.setCellValue(idHeaderName);
    if (headerStyle != null) {
      idHeaderCell.setCellStyle(headerStyle);
    }

    // Cập nhật vị trí cột label mới
    labelPos[1] = labelCol + 1;

    return new int[] {headerRowIdx, labelCol};
  }

  /** Tìm chỉ số cột cuối cùng trong sheet. */
  private static int findLastColumn(Sheet sheet) {
    int maxCol = 0;
    for (int r = 0; r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row != null && row.getLastCellNum() > maxCol) {
        maxCol = row.getLastCellNum();
      }
    }
    return maxCol > 0 ? maxCol - 1 : 0;
  }

  private static Sheet createOptionSheet(Workbook workbook, String sheetName) {
    String safeName = sanitizeSheetName(sheetName);
    String finalName = safeName;
    int suffix = 1;
    while (workbook.getSheet(finalName) != null) {
      finalName = safeName + "_" + suffix++;
    }
    return workbook.createSheet(finalName);
  }

  private static String sanitizeSheetName(String name) {
    String sanitized = name.replaceAll(SHEET_NAME_INVALID_CHARS_PATTERN, "_");
    if (sanitized.length() > 31) {
      sanitized = sanitized.substring(0, 31);
    }
    return sanitized;
  }

  /** Ghi Label vào cột A, Value vào cột B */
  private static void writeOptions(Sheet sheet, List<DropdownOption> options) {
    for (int rowIdx = 0; rowIdx < options.size(); rowIdx++) {
      DropdownOption opt = options.get(rowIdx);
      Row row = sheet.createRow(rowIdx);
      row.createCell(0).setCellValue(opt.getLabel());
      Object val = opt.getValue();
      if (val != null) {
        if (val instanceof Number) {
          row.createCell(1).setCellValue(((Number) val).doubleValue());
        } else {
          row.createCell(1).setCellValue(val.toString());
        }
      }
    }
  }

  private static void createNamedRanges(
      Workbook wb,
      Sheet sheet,
      String labelName,
      String mapName,
      int count,
      boolean isMappingValue) {
    String sheetName = sheet.getSheetName();

    removeNameIfExists(wb, labelName);
    Name lblRange = wb.createName();
    lblRange.setNameName(labelName);
    lblRange.setRefersToFormula(String.format(LABEL_RANGE_FORMULA, sheetName, count));

    if (isMappingValue && mapName != null) {
      removeNameIfExists(wb, mapName);
      Name mapRange = wb.createName();
      mapRange.setNameName(mapName);
      mapRange.setRefersToFormula(String.format(MAPPING_RANGE_FORMULA, sheetName, count));
    }
  }

  private static void removeNameIfExists(Workbook wb, String name) {
    Name existing = wb.getName(name);
    if (existing != null) {
      wb.removeName(existing);
    }
  }

  private static void applyDataValidation(
      Sheet sheet, String rangeName, int col, int headerRow, int lastRow) {
    DataValidationHelper helper = sheet.getDataValidationHelper();
    DataValidationConstraint constraint = helper.createFormulaListConstraint(rangeName);
    int startRow = headerRow + 1;
    int endRow = Math.max(lastRow, startRow) + DROPDOWN_ROW_BUFFER;
    CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, col, col);
    DataValidation validation = helper.createValidation(constraint, addressList);
    validation.setShowErrorBox(true);
    sheet.addValidationData(validation);
  }

  /** Thiết lập công thức VLOOKUP: =IFERROR(VLOOKUP(LabelCell, MappingRange, 2, FALSE), "") */
  private static void applyVLookupFormula(
      Sheet sheet, String mapRange, int labelCol, int idCol, int headerRow, int lastRow) {
    int startRow = headerRow + 1;
    int endRow = Math.max(lastRow, startRow) + DROPDOWN_ROW_BUFFER;

    for (int r = startRow; r <= endRow; r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        row = sheet.createRow(r);
      }
      Cell idCell = row.createCell(idCol);

      // Lấy địa chỉ ô Label tương ứng (ví dụ: E10)
      String labelCellRef = new CellReference(r, labelCol).formatAsString();
      String formula = String.format(VLOOKUP_FORMULA, labelCellRef, mapRange);
      idCell.setCellFormula(formula);
    }
  }

  private static int[] findHeaderPosition(Sheet sheet, String headerName) {
    for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
      Row row = sheet.getRow(rowIdx);
      if (row == null) {
        continue;
      }
      for (int colIdx = 0; colIdx < row.getLastCellNum(); colIdx++) {
        Cell cell = row.getCell(colIdx);
        if (cell != null
            && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
            && headerName.equals(cell.getStringCellValue().trim())) {
          return new int[] {rowIdx, colIdx};
        }
      }
    }
    return null;
  }
}
