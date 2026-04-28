package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.aware.PageResultAware;
import com.eps.shared.core.constants.ParamsKeys;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.query.SearchQuery;
import com.eps.shared.core.json.JsonParserUtils;
import com.eps.shared.core.result.PageResponse;
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

public interface GetAllApi<E, R> extends MediatorAware, EntityAware<E>, PageResultAware<R> {

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
        description = "Số trang (bắt đầu từ 1)",
        schema = @Schema(type = "integer", defaultValue = "1")),
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
  default PageResponse<R> getAll(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @RequestParam(required = false, name = "search") String search,
      @RequestParam(required = false, name = "page", defaultValue = "1") Integer page,
      @RequestParam(required = false, name = "page_size", defaultValue = "20") Integer pageSize,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "filter") String filter) {

    if (getMediator() == null) {
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
        getMediator()
            .query(
                context,
                new SearchQuery<>(
                    getEntityClass(),
                    search,
                    page,
                    pageSize,
                    sort,
                    filterMap,
                    getPageResultClass())));
  }
}
