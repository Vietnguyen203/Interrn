package com.vietnl.sharedlibrary.core.cqrs;

public abstract class CqrsBaseService<E, ID, C, R, PAGE_R> extends CqrsCrudService<E, ID, C, R>
    implements CqrsSearchService<E, PAGE_R> {}
