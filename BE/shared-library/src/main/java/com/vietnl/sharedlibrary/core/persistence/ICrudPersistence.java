package com.vietnl.sharedlibrary.core.persistence;

import java.util.List;
import java.util.Optional;

public interface ICrudPersistence<E, ID> {
  E save(E entity);

  <T extends E> List<T> saveAll(Iterable<T> entities);

  Optional<E> findById(ID id);

  Optional<E> findFirstByOrderByIdDesc();

  default E findByIdOrNull(ID id) {

    return findById(id).orElse(null);
  }

  boolean existsById(ID id);

  List<E> findAllByIdIn(Iterable<ID> ids);

  void deleteById(ID id);

  void deleteByIdIn(Iterable<ID> id);

  void delete(E entity);

  void deleteAllByIdIn(Iterable<? extends ID> ids);

  void deleteAll(Iterable<? extends E> entities);
}
