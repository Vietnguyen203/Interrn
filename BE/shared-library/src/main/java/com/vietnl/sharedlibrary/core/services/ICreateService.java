package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.services.v2.CreateService;
import com.eps.shared.core.utils.GenericTypeUtils;
import com.eps.shared.core.valueobject.PositionType;
import com.github.f4b6a3.uuid.UuidCreator;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

public interface ICreateService<E, ID, RES, REQ>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID>, CreateService<REQ, RES> {
  /**
   * Tạo entity mới từ request và lưu vào database. Có hỗ trợ validation và mapping tùy chỉnh.
   *
   * @param context Header context (chứa thông tin người dùng, locale, ...)
   * @param request Dữ liệu từ client gửi lên
   * @param validationCreateHandler Hàm callback kiểm tra dữ liệu đầu vào (có thể throw lỗi)
   * @param mappingEntityHandler Hàm callback để gán dữ liệu tùy chỉnh vào entity
   * @return Entity sau khi được lưu vào database
   */
  @Transactional
  default RES create(
      HeaderContext context,
      REQ request,
      TriConsumer<HeaderContext, E, REQ> validationCreateHandler,
      TriConsumer<HeaderContext, E, REQ> mappingEntityHandler,
      TriConsumer<HeaderContext, E, REQ> mappingAuditingEntityHandler,
      TriConsumer<HeaderContext, E, REQ> postHandler,
      BiFunction<HeaderContext, E, RES> mappingResponseHandler) {

    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("createPersistence must not be null");
    }

    E entity =
        GenericTypeUtils.getNewInstance(
            this, ICreateService.class, PositionType.FIRST); // Tạo entity mới bằng Reflection

    if (validationCreateHandler != null) {
      validationCreateHandler.accept(context, entity, request);
    }

    if (mappingAuditingEntityHandler != null) {
      mappingAuditingEntityHandler.accept(context, entity, request); // Gọi mapping tùy chỉnh
    }

    if (mappingEntityHandler != null) {
      mappingEntityHandler.accept(context, entity, request); // Gọi mapping tùy chỉnh
    }

    entity = getCrudPersistence().save(entity);

    postHandler.accept(context, entity, request);

    if (mappingResponseHandler == null) {
      throw new IllegalArgumentException("mappingResponseHandler must not be null");
    }
    return mappingResponseHandler.apply(context, entity); // Lưu entity vào DB
  }

  @Override
  @Transactional
  /** Hàm tạo mặc định nếu không truyền vào hàm validate/mapping riêng. */
  default RES create(HeaderContext context, REQ request) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("createPersistence must not be null");
    }
    return create(
        context,
        request,
        this::validateCreateRequest,
        this::mappingCreateAuditingEntity,
        this::mappingCreateEntity,
        this::postCreateHandler,
        this::mappingResponse);
  }

  /** Hàm validate mặc định (không làm gì) — override trong implementation nếu cần. */
  default void validateCreateRequest(HeaderContext context, E entity, REQ request) {}

  /** Hàm mapping mặc định (không làm gì) — override nếu cần xử lý riêng. */
  default void mappingCreateEntity(HeaderContext context, E entity, REQ request) {
    FnCommon.copyProperties(entity, request); // Copy các field giống nhau từ request sang entity
  }

  default void postCreateHandler(HeaderContext context, E entity, REQ request) {}

  default void mappingCreateAuditingEntity(HeaderContext context, E entity, REQ request) {
    if (context != null) {
      if (GenericTypeUtils.getFieldValue(entity, "id") == null) {
        GenericTypeUtils.updateData(entity, "id", UuidCreator.getTimeOrderedEpoch());
      }
      GenericTypeUtils.updateData(entity, "createdBy", context.getUserCode());
      GenericTypeUtils.updateData(entity, "updatedBy", context.getUserCode());
    }
  }
}
