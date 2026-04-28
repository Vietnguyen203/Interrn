package com.vietnl.sharedlibrary.controller;

import com.eps.shared.core.services.*;

/**
 * Interface định nghĩa chuẩn RESTful cho các controller CRUD (Create, Read, Update, Delete). Có thể
 * áp dụng lại cho nhiều entity khác nhau nhằm giảm lặp code.
 *
 * @param <RES> Kiểu dữ liệu trả về (Response DTO)
 * @param <ID> Kiểu ID của entity (VD: Long, UUID)
 * @param <REQ> Kiểu dữ liệu request đầu vào (Request DTO)
 */
public interface ICrudApi<E, ID, RES, REQ>
    extends ICreateApi<E, ID, RES, REQ>,
        IUpdateApi<E, ID, RES, REQ>,
        IPatchApi<E, ID, RES, REQ>,
        IDeleteApi<E, ID>,
        IGetApi<E, ID, RES>,
        IDeleteBatchApi<E, ID> {

  ICrudService<E, ID, RES, REQ> getCrudService();

  @Override
  default ICreateService<E, ID, RES, REQ> getCreateService() {
    return getCrudService();
  }

  @Override
  default IUpdateService<E, ID, RES, REQ> getUpdateService() {
    return getCrudService();
  }

  @Override
  default IDeleteService<E, ID> getDeleteService() {
    return getCrudService();
  }

  @Override
  default IGetService<E, ID, RES> getGetService() {
    return getCrudService();
  }

  @Override
  default IPatchService<E, ID, RES, REQ> getPatchService() {
    return getCrudService();
  }
}
