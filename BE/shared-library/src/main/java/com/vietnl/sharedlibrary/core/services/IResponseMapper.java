package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.utils.GenericTypeUtils;
import com.eps.shared.core.valueobject.PositionType;

public interface IResponseMapper<E, RES> {

  default RES mappingResponse(HeaderContext context, E entity) {

    RES res = GenericTypeUtils.getNewInstance(this, IResponseMapper.class, PositionType.LAST);

    FnCommon.copyProperties(res, entity);

    return res;
  }
}
