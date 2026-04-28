package com.vietnl.sharedlibrary.controller.v2;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.aware.ResultAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.MediatorAware;
import com.eps.shared.core.cqrs.query.GetQuery;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface GetApi<E, ID, R> extends MediatorAware, EntityAware<E>, ResultAware<R> {

  @GetMapping(path = "/{id}")
  default R getById(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @PathVariable(name = "id") ID id) {
    if (getMediator() == null) {
      return null;
    }
    return getMediator().query(context, new GetQuery<>(getEntityClass(), id, getResultClass()));
  }
}
