package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.IUpdateService;
import com.eps.shared.core.valueobject.OnUpdate;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface IUpdateApi<E, ID, RES, REQ> {

  IUpdateService<E, ID, RES, REQ> getUpdateService();

  /**
   * Cập nhật một entity theo ID.
   *
   * @param context HeaderContext từ request
   * @param id ID của entity cần cập nhật
   * @param request Dữ liệu cập nhật
   * @return RES Đối tượng đã cập nhật
   */
  @PutMapping(path = "/{id}")
  default ResponseEntity<RES> update(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id,
      @RequestBody @Validated(OnUpdate.class) REQ request) {
    if (getUpdateService() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    return ResponseEntity.ok(getUpdateService().update(context, id, request));
  }
}
