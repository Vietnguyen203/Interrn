package com.vietnl.sharedlibrary.core.excel.exportExcel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation dùng để cấu hình dropdown (Data Validation) cho cột Excel dựa trên field của DTO. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDropdown {

  /** Tên header của cột hiển thị dropdown cho người dùng chọn. */
  String headerName();

  /**
   * (Tùy chọn) Tên header của cột ẩn chứa giá trị thực tế (ID). Nếu có, hệ thống sẽ tự động tạo
   * công thức VLOOKUP để map Name -> ID.
   */
  String value() default "";

  /** Class Enum để lấy danh sách giá trị. */
  Class<? extends Enum<?>> enumClass() default DefaultEnum.class;

  /** Key để định danh nguồn dữ liệu động. */
  String data() default "";

  /** Danh sách options cố định (chỉ dành cho label=value). */
  String[] options() default {};

  enum DefaultEnum {}
}
