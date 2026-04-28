package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.services.IGetService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

public interface IGetApi<E, ID, RES> {

  IGetService<E, ID, RES> getGetService();

  /**
   * Lấy thông tin một entity theo ID.
   *
   * @param context HeaderContext từ request
   * @param id ID của entity cần lấy
   * @return RES Thông tin entity
   */
  @GetMapping(path = "/{id}")
  default RES getById(
      @Parameter(hidden = true) HeaderContext context,
      @RequestHeader Map<String, Object> headers,
      @Parameter(description = "ID of the user to retrieve", required = true)
          @PathVariable(name = "id")
          ID id) {
    if (getGetService() == null) {
      return null;
    }
    return getGetService().getById(context, id);
  }
}
