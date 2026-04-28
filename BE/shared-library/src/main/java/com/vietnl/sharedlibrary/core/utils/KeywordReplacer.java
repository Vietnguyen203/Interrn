package com.vietnl.sharedlibrary.core.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordReplacer {

  private static final Pattern PATH_KEYWORD_PATTERN = Pattern.compile("(\\w+)");

  public static String replaceKeywords(
      String input, String patternString, Map<String, String> replacements) {
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(input);
    StringBuilder result = new StringBuilder();

    int lastEnd = 0;
    while (matcher.find()) {
      result.append(input, lastEnd, matcher.start());

      String key = matcher.group(1); // lấy từ khóa không có dấu #
      String replacement =
          replacements.getOrDefault(key, matcher.group(0)); // giữ nguyên nếu không có trong map

      result.append(replacement);
      lastEnd = matcher.end();
    }

    result.append(input.substring(lastEnd)); // phần còn lại sau match cuối cùng
    return result.toString();
  }

  public static String replaceKeywords(String input, Map<String, String> replacements) {
    return replaceKeywords(input, "#(\\w+)#", replacements);
  }
}
