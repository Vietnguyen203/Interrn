package com.vietnl.sharedlibrary.core.services.v2;

public interface CrudService<ID, C, R>
    extends CreateService<C, R>,
        UpdateService<ID, C, R>,
        PatchService<ID, C, R>,
        GetService<ID, R>,
        DeleteService<ID> {}
