package com.vietnl.sharedlibrary.core.constants;

import com.eps.shared.core.utils.StringUtils;

public class ParamsKeys {

  public static final String SEARCH = "search";
  public static final String PAGE = "page";
  public static final String SIZE = "page_size";
  public static final String SORT = "sort";
  public static final String FILTER = "filter";

  public static final String PREFIX_FROM = "from";
  public static final String PREFIX_TO = "to";

  public static String getFieldName(String prefix, String field) {

    return prefix + "." + StringUtils.capitalize(field);
  }
}
