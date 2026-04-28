package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.persistence.IBasePersistence;
import com.eps.shared.core.persistence.ICrudPersistence;
import com.eps.shared.core.persistence.IJpaGetAllPersistence;

public interface IBaseService<E, ID, RES, REQ, PAGE_RES>
    extends ICrudService<E, ID, RES, REQ>, IGetAllService<E, PAGE_RES> {

  IBasePersistence<E, ID> getPersistence();

  @Override
  default ICrudPersistence<E, ID> getCrudPersistence() {
    return getPersistence();
  }

  @Override
  default IJpaGetAllPersistence<E> getJpaGetAllPersistence() {
    return getPersistence();
  }
}
