package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.command.UpdateCommand;
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

public interface UpdateApi<E, ID, C> extends MediatorAware, EntityAware<E> {

  @PutMapping(path = "/{id}")
  default ResponseEntity<?> update(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id,
      @RequestBody @Validated(OnUpdate.class) C request) {
    if (getMediator() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    return ResponseEntity.ok(
        getMediator().send(context, new UpdateCommand<>(getEntityClass(), id, request)));
  }
}
