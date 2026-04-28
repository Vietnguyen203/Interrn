package com.vietnl.sharedlibrary.security.authorization;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RolesAllowed {
  String[] value();
}
