package com.vietnl.sharedlibrary.observability.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

  Class<? extends Enum<?>> enumClass();

  String value();

  String note() default "";

  String input() default ""; // SpEL: "#uuid, #request"
}
