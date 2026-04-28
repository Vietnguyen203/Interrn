package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.IPatchService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IPatchApi<E, ID, RES, REQ> {

  IPatchService<E, ID, RES, REQ> getPatchService();

  /**
   * Cập nhật một entity theo ID.
   *
   * @param context HeaderContext từ request
   * @param id ID của entity cần cập nhật
   * @param request Dữ liệu cập nhật
   * @return RES Đối tượng đã cập nhật
   */
  @PatchMapping(path = "/{id}")
  default ResponseEntity<RES> patch(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id,
      @RequestBody REQ request) {
    if (getPatchService() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    return ResponseEntity.ok(getPatchService().patch(context, id, request));
  }
}
