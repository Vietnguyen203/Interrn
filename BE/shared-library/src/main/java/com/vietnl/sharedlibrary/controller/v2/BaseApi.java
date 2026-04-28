package com.vietnl.sharedlibrary.controller.v2;

public interface BaseApi<E, ID, C, R, PAGE_R> extends GetAllApi<E, PAGE_R>, CrudApi<E, ID, C, R> {}
