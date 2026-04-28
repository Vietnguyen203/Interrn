package com.vietnl.sharedlibrary.core.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public interface IJpaGetAllPersistence<E> {

  Page<E> findAll(@Nullable Specification<E> spec, Pageable pageable);
}
