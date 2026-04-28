package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.services.IBaseService;
import com.eps.shared.core.services.ICrudService;
import com.eps.shared.core.services.IGetAllService;

public interface IBaseApi<E, ID, RES, REQ, PAGE_RES>
    extends IGetAllApi<E, PAGE_RES>, ICrudApi<E, ID, RES, REQ> {

  IBaseService<E, ID, RES, REQ, PAGE_RES> getService();

  @Override
  default ICrudService<E, ID, RES, REQ> getCrudService() {
    return getService();
  }

  @Override
  default IGetAllService<E, PAGE_RES> getGetAllService() {
    return getService();
  }
}
