package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.core.services.v2.GetService;
import org.springframework.http.HttpStatus;

public interface IGetService<E, ID, RES>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID>, GetService<ID, RES> {

  @Override
  /** Tìm entity theo ID, ném lỗi 404 nếu không tìm thấy. */
  default RES getById(HeaderContext context, ID id) {

    E entity = getCrudPersistence().findByIdOrNull(id);

    if (entity == null) {
      throw new ResponseException(HttpStatus.NOT_FOUND, CommonErrorMessage.OBJECT_NOT_FOUND);
    }

    return mappingResponse(context, entity);
  }
}
