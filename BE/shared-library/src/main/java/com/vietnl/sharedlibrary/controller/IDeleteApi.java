package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.IDeleteService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface IDeleteApi<E, ID> {

  IDeleteService<E, ID> getDeleteService();

  /**
   * Xoá một entity theo ID.
   *
   * @param context HeaderContext từ request
   * @param id ID của entity cần xoá
   */
  @DeleteMapping(path = "/{id}")
  default ResponseEntity<?> delete(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id) {
    if (getDeleteService() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    getDeleteService().delete(context, id);
    return ResponseEntity.noContent().build();
  }
}
