package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.services.v2.CrudService;

public abstract class CqrsCrudService<E, ID, C, R>
    implements CqrsCreateService<E, C, R>,
        CqrsUpdateService<E, ID, C, R>,
        CqrsDeleteService<E, ID>,
        CqrsGetService<E, ID, R>,
        CrudService<ID, C, R> {}
