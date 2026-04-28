package com.vietnl.sharedlibrary.core.persistence;

public interface IBasePersistence<E, ID>
    extends ICrudPersistence<E, ID>, IJpaGetAllPersistence<E> {}
