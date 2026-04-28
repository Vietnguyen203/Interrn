package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.services.v2.PatchService;
import com.eps.shared.core.utils.GenericTypeUtils;
import com.eps.shared.core.utils.functions.PentaConsumer;
import com.eps.shared.core.utils.functions.QuadConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

public interface IPatchService<E, ID, RES, REQ>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID>, PatchService<ID, REQ, RES> {
  /**
   * Cập nhật entity hiện có theo ID, có validate và mapping tuỳ chỉnh.
   *
   * @param context Header context
   * @param id ID của entity
   * @param request Request chứa thông tin cần cập nhật
   * @param validationHandler Hàm kiểm tra hợp lệ trước khi update
   * @param mappingHandler Hàm mapping dữ liệu request vào entity
   * @return Entity đã cập nhật
   */
  @Transactional
  default RES patch(
      HeaderContext context,
      ID id,
      REQ request,
      QuadConsumer<HeaderContext, ID, E, REQ> validationHandler,
      TriConsumer<HeaderContext, E, REQ> mappingPatchAuditingEntity,
      TriConsumer<HeaderContext, E, REQ> mappingHandler,
      PentaConsumer<HeaderContext, E, E, ID, REQ> postHandler,
      BiFunction<HeaderContext, E, RES> mappingResponseHandler) {

    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("updatePersistence must not be null");
    }
    E entity =
        getCrudPersistence().findByIdOrNull(id); // Lấy entity từ DB, nếu không có thì ném lỗi 404

    E originalEntity = (E) FnCommon.copyProperties(entity.getClass(), entity);

    if (validationHandler != null) {
      validationHandler.accept(context, id, entity, request); // Kiểm tra hợp lệ
    }

    if (mappingPatchAuditingEntity != null) {
      mappingPatchAuditingEntity.accept(context, entity, request); // Gọi hàm mapping tùy chỉnh
    }

    if (mappingHandler != null) {
      mappingHandler.accept(context, entity, request); // Gọi hàm mapping tùy chỉnh
    }

    entity = getCrudPersistence().save(entity);

    if (postHandler != null) {
      postHandler.accept(context, originalEntity, entity, id, request);
    }

    if (mappingResponseHandler == null) {
      throw new IllegalArgumentException("mappingResponseHandler must not be null");
    }

    return mappingResponseHandler.apply(context, entity); // Lưu lại vào DB
  }

  @Transactional
  /** Cập nhật mặc định nếu không cần validate/mapping riêng. */
  default RES patch(HeaderContext context, ID id, REQ request) {

    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("updatePersistence must not be null");
    }

    return patch(
        context,
        id,
        request,
        this::validatePatchRequest,
        this::mappingPatchAuditingEntity,
        this::mappingPatchEntity,
        this::postPatchHandler,
        this::mappingResponse);
  }

  // Validate mặc định khi update
  default void validatePatchRequest(HeaderContext context, ID id, E entity, REQ request) {}

  // Mapping mặc định khi update
  default void mappingPatchEntity(HeaderContext context, E entity, REQ request) {
    FnCommon.copyNotNullProperties(entity, request); // Gán dữ liệu chung từ request
  }

  default void postPatchHandler(
      HeaderContext context, E originalEntity, E entity, ID id, REQ request) {}

  default void mappingPatchAuditingEntity(HeaderContext context, E entity, REQ request) {
    if (context != null) {
      GenericTypeUtils.updateData(entity, "updatedBy", context.getUserCode());
    }
  }
}
