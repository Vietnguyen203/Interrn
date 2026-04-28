package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.command.BulkDeleteCommand;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface DeleteBatchApi<E, ID> extends MediatorAware, EntityAware<E> {

  @DeleteMapping(path = "/batch")
  default ResponseEntity<Void> deleteBatch(
      @Parameter(hidden = true) HeaderContext context,
      @RequestBody BatchDeleteRequest<ID> payload) {
    if (getMediator() == null) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    getMediator().send(context, new BulkDeleteCommand<>(getEntityClass(), payload.getIds()));
    return ResponseEntity.noContent().build();
  }

  @Setter
  @Getter
  class BatchDeleteRequest<ID> {
    private List<ID> ids;
  }
}
