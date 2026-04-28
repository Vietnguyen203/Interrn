package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.command.PatchCommand;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface PatchApi<E, ID, C> extends MediatorAware, EntityAware<E> {

  @PatchMapping(path = "/{id}")
  default ResponseEntity<?> patch(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id,
      @RequestBody C request) {
    if (getMediator() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    return ResponseEntity.ok(
        getMediator().send(context, new PatchCommand<>(getEntityClass(), id, request)));
  }
}
