package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.command.DeleteCommand;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface DeleteApi<E, ID> extends MediatorAware, EntityAware<E> {

  @DeleteMapping(path = "/{id}")
  default ResponseEntity<Void> delete(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id) {
    if (getMediator() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    getMediator().send(context, new DeleteCommand<>(getEntityClass(), id));
    return ResponseEntity.noContent().build();
  }
}
