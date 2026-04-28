package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.constants.ParamsKeys;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.json.JsonParserUtils;
import com.eps.shared.core.result.PageResponse;
import com.eps.shared.core.services.IGetAllService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

public interface IGetAllApi<E, RES> {

  IGetAllService<E, RES> getGetAllService();

  @GetMapping
  @Parameters({
    @Parameter(
        name = ParamsKeys.FILTER,
        in = ParameterIn.QUERY,
        description =
            "JSON string chứa object filter. Ví dụ: {\"status\":\"ACTIVE\", \"category\":\"BOOK\"}",
        schema = @Schema(type = "string", format = "json")),
    @Parameter(
        name = "page",
        in = ParameterIn.QUERY,
        description = "Số trang (bắt đầu từ 0)",
        schema = @Schema(type = "integer", defaultValue = "0")),
    @Parameter(
        name = "page_size",
        in = ParameterIn.QUERY,
        description = "Kích thước trang",
        schema = @Schema(type = "integer", defaultValue = "20")),
    @Parameter(
        name = "sort",
        in = ParameterIn.QUERY,
        description = "JSON dạng: {\"createdAt\": -1, \"updatedAt\": 1} (1 = ASC, -1 = DESC)",
        required = false,
        schema =
            @Schema(
                type = "string",
                format = "json",
                example = "{\"createdAt\": -1, \"updatedAt\": 1}"))
  })
  default PageResponse<RES> getAll(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @RequestParam(required = false, name = "search") String search,
      @RequestParam(required = false, name = "page", defaultValue = "1") Integer page,
      @RequestParam(required = false, name = "page_size", defaultValue = "20") Integer pageSize,
      @RequestParam(required = false, name = "sort") String sort,
      //      @Parameter(hidden = true) Pageable pageable,
      @RequestParam(required = false, name = "filter") String filter) {

    if (getGetAllService() == null) {
      return null;
    }

    Map<String, Object> filterMap = new HashMap<>();

    if (StringUtils.hasLength(filter)) {
      try {
        filterMap = JsonParserUtils.entity(filter.trim(), Map.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return new PageResponse<>(
        getGetAllService().getAll(context, search, page, pageSize, sort, filterMap));
  }
}
