package ${PACKAGE}.application.usecases;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import com.eps.shared.utils.FnCommon;
import com.eps.shared.utils.PageableUtils;
import com.eps.user.application.requests.${ENTITY}Request;
import com.eps.user.application.responses.${ENTITY}PageResponse;
import com.eps.user.application.validators.${ENTITY}Validator;
import com.eps.user.domain.models.entities.${ENTITY};
import com.eps.user.domain.models.enums.ExceptionMessage;
import com.eps.user.domain.models.enums.${ENTITY}Status;
import com.eps.user.domain.services.persistence.${ENTITY}Persistence;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ${ENTITY}Service
    implements IBaseService<
        ${ENTITY}, UUID, ${ENTITY}PageResponse, ${ENTITY}Request, ${ENTITY}PageResponse> {

  private final ${ENTITY}Persistence persistence;
  private final ${ENTITY}Validator validator;

  @Override
  public IBasePersistence<${ENTITY}, UUID> getPersistence() {
    return persistence;
  }

  @Override
  public void validateCreateRequest(
      HeaderContext context, ${ENTITY} entity, ${ENTITY}Request request) {
    validator.validateCreate(request);
  }

  @Override
  public void validateUpdateRequest(
      HeaderContext context, UUID id, ${ENTITY} entity, ${ENTITY}Request request) {
    validator.validateUpdate(id, request);
  }

  @Override
  public String[] getSearchFieldNames() {
    return new String[] {"name", "code"};
  }

  @Override
  public void mappingCreateEntity(
      HeaderContext context, ${ENTITY} entity, ${ENTITY}Request request) {
    IBaseService.super.mappingCreateEntity(context, entity, request);

    if (entity.getCode() != null) {
      entity.setCode(entity.getCode().trim().toUpperCase());
    }
  }

  @Override
  public ${ENTITY}PageResponse mappingResponse(HeaderContext context, ${ENTITY} entity) {
    ${ENTITY}PageResponse response = new ${ENTITY}PageResponse();
    FnCommon.copyProperties(response, entity);
    return response;
  }

  @Override
  public Page<${ENTITY}PageResponse> mappingPageResponse(
      HeaderContext context, Page<${ENTITY}> items) {
    return items.map(
        item -> {
          ${ENTITY}PageResponse dto = new ${ENTITY}PageResponse();
          FnCommon.copyProperties(dto, item);
          return dto;
        });
  }

  @Transactional
  public void changeStatus(HeaderContext context, ${ENTITY}Status status, UUID uuid) {
    ${ENTITY} entity = persistence.findByIdOrNull(uuid);
    if (entity == null) {
      throw new ResponseException(HttpStatus.NOT_FOUND, ExceptionMessage.${ENTITY_UPPER}_NOT_FOUND);
    }

    if (entity.getStatus().equals(status)) {
      return;
    }
    entity.setStatus(status);
    persistence.save(entity);
  }

  public Page<${ENTITY}PageResponse> search(
      Integer page, Integer pageSize, String sort, Map<String, Object> filter) {
    Pageable pageable = PageableUtils.convertPageable(page, pageSize, sort);
    return getJpaGetAllPersistence().findAll(buildSpecification(filter), pageable)
        .map(this::mappingResponse);
  }
}