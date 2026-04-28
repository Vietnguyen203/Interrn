package com.vietnl.sharedlibrary.controller.v2;

public interface CrudApi<E, ID, C, R>
    extends CreateApi<E, C>,
        UpdateApi<E, ID, C>,
        PatchApi<E, ID, C>,
        GetApi<E, ID, R>,
        DeleteApi<E, ID>,
        DeleteBatchApi<E, ID> {}
