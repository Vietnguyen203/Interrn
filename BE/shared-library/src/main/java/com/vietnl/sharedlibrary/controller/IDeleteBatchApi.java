package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.IDeleteService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

public interface IDeleteBatchApi<E, ID> {

  IDeleteService<E, ID> getDeleteService();

  @DeleteMapping(path = "/batch")
  default ResponseEntity<?> deleteBatch(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @RequestBody BatchDeleteRequest<ID> payload) {
    if (getDeleteService() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    getDeleteService().deleteBatch(context, payload.getIds());
    return ResponseEntity.noContent().build();
  }

  @Setter
  @Getter
  class BatchDeleteRequest<ID> {
    private List<ID> ids;
  }
}
