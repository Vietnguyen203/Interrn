package ${PACKAGE}.adapter.apis;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.values.response.PageResponse;
import com.eps.shared.utils.HeaderKeys;
import com.eps.shared.utils.JsonParserUtils;
import com.eps.user.application.requests.${ENTITY}Request;
import com.eps.user.application.responses.${ENTITY}PageResponse;
import com.eps.user.application.usecases.${ENTITY}Service;
import com.eps.user.domain.models.entities.${ENTITY};
import com.eps.user.domain.models.enums.${ENTITY}Status;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/${ENTITY_LOWER}")
@RequiredArgsConstructor
@RestController
public class ${ENTITY}Api
    implements IBaseApi<
        ${ENTITY}, UUID, ${ENTITY}PageResponse, ${ENTITY}Request, ${ENTITY}PageResponse> {

  private final ${ENTITY}Service service;

  @Override
  public IBaseService<
          ${ENTITY}, UUID, ${ENTITY}PageResponse, ${ENTITY}Request, ${ENTITY}PageResponse>
      getService() {
    return service;
  }

  @Override
  public ResponseEntity<${ENTITY}PageResponse> create(
      HeaderContext context, Map<String, Object> headers, ${ENTITY}Request request) {
    context
        .getExtraData()
        .put(HttpHeaders.AUTHORIZATION, (String) headers.get(HeaderKeys.AUTHORIZATION));
    return IBaseApi.super.create(context, headers, request);
  }

  @Override
  public ResponseEntity<${ENTITY}PageResponse> update(
      HeaderContext context, Map<String, Object> headers, UUID uuid, ${ENTITY}Request request) {
    context
        .getExtraData()
        .put(HttpHeaders.AUTHORIZATION, (String) headers.get(HeaderKeys.AUTHORIZATION));
    return IBaseApi.super.update(context, headers, uuid, request);
  }

  @PatchMapping(path = "/{id}/status/{status}")
  public ResponseEntity<?> changeStatus(
      HeaderContext context,
      @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable(name = "id") UUID uuid,
      @PathVariable(name = "status") ${ENTITY}Status status) {
    context.getExtraData().put(HttpHeaders.AUTHORIZATION, authorization);

    service.changeStatus(context, status, uuid);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  public ResponseEntity<?> search(
      @RequestParam(required = false, name = "page", defaultValue = "1") Integer page,
      @RequestParam(required = false, name = "page_size", defaultValue = "20") Integer pageSize,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "filter") String filter) {

    Map<String, Object> filterMap = new HashMap<>();

    if (StringUtils.hasLength(filter)) {
      try {
        filterMap = JsonParserUtils.entity(filter.trim(), Map.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return ResponseEntity.ok(
        new PageResponse<>(
            service.search(page, pageSize, sort, filterMap)));
  }
}