package com.vietnl.sharedlibrary.core.utils;

import com.eps.shared.core.json.JsonParserUtils;
import com.eps.shared.core.valueobject.SortEnum;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageableUtils {
  public static Pageable convertPageable(Integer page, Integer size, List<Sort.Order> orders) {
    Sort sort = Sort.unsorted();
    if (!orders.isEmpty()) {
      sort = Sort.by(orders);
    }
    if (page == 0 || size == 0) {
      return Pageable.unpaged(sort);
    } else {
      return PageRequest.of(page - 1, size, sort);
    }
  }

  public static Pageable convertPageable(Integer pageStr, Integer sizeStr, String sortStr) {
    // Handle sort
    Map<String, String> sortMap = JsonParserUtils.toStringMap(sortStr);

    List<Sort.Order> orders =
        sortMap.entrySet().stream()
            .map(
                entry -> {
                  Sort.Direction direction =
                      Sort.Direction.fromString(
                          SortEnum.fromOriginal(NumberUtils.parseInt(entry.getValue(), 1)).name());
                  return new Sort.Order(direction, entry.getKey());
                })
            .collect(Collectors.toList());

    int page = NumberUtils.parseInt(String.valueOf(pageStr), 0);
    int size = NumberUtils.parseInt(String.valueOf(sizeStr), 20);
    //    size = Math.min(size, properties.getMaxPageSize());

    return convertPageable(page, size, orders);
  }
}
