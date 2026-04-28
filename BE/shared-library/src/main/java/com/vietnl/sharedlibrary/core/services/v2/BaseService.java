package com.vietnl.sharedlibrary.core.services.v2;

public interface BaseService<ID, C, R, PAGE_RES>
    extends CrudService<ID, C, R>, GetAllService<PAGE_RES> {}
