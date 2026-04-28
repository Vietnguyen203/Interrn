package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.v2.DeleteService;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IDeleteService<E, ID> extends ICrudPersistenceProvider<E, ID>, DeleteService<ID> {

  /** Xóa entity theo ID, có thể validate trước khi xóa. */
  @Transactional
  default void delete(
      HeaderContext context,
      ID id,
      TriConsumer<HeaderContext, ID, E> validationHandler,
      TriConsumer<HeaderContext, ID, E> postDeleteHandler) {
    E entity = getCrudPersistence().findByIdOrNull(id); // Lấy entity từ DB

    if (validationHandler != null) {
      validationHandler.accept(context, id, entity); // Kiểm tra hợp lệ trước khi xóa
    }
    postDeleteHandler.accept(context, id, entity);
    getCrudPersistence().delete(entity); // Xóa khỏi DB
  }

  @Override
  @Transactional
  /** Hàm xóa mặc định không cần validate riêng. */
  default void delete(HeaderContext context, ID id) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("deletePersistence must not be null");
    }

    delete(context, id, this::validateDelete, this::postDeleteHandler);
  }

  // Hàm validate mặc định khi xóa
  default void validateDelete(HeaderContext context, ID id, E entity) {}

  default void postDeleteHandler(HeaderContext context, ID id, E entity) {}

  /** Xóa nhiều entity theo danh sách ID. */
  @Transactional
  default void deleteBatch(
      HeaderContext context,
      List<ID> ids,
      TriConsumer<HeaderContext, List<ID>, List<E>> validationHandler,
      TriConsumer<HeaderContext, List<ID>, List<E>> postDeleteHandler) {
    List<E> entities = getCrudPersistence().findAllByIdIn(ids); // Lấy danh sách entity

    if (validationHandler != null) {
      validationHandler.accept(context, ids, entities); // Validate
    }
    postDeleteHandler.accept(context, ids, entities);
    getCrudPersistence().deleteAll(entities); // Xóa danh sách
  }

  @Override
  @Transactional
  /** Hàm xóa nhiều mặc định không cần validate riêng. */
  default void deleteBatch(HeaderContext context, List<ID> ids) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("deletePersistence must not be null");
    }
    deleteBatch(context, ids, this::validateDeleteBatch, this::postDeleteBatchHandler);
  }

  // Hàm validate mặc định khi xóa nhiều
  default void validateDeleteBatch(HeaderContext context, List<ID> ids, List<E> entities) {}

  default void postDeleteBatchHandler(HeaderContext context, List<ID> ids, List<E> entities) {}
}
