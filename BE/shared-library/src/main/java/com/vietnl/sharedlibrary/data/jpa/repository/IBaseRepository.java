package com.vietnl.sharedlibrary.data.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBaseRepository<E, ID> extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {}
