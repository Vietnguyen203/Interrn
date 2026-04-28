package com.vietnl.sharedlibrary.core.aware;

import com.eps.shared.core.persistence.ICrudPersistence;

public interface ICrudPersistenceProvider<E, ID> {

  ICrudPersistence<E, ID> getCrudPersistence();
}
