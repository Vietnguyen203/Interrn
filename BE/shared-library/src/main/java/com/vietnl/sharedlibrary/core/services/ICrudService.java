package com.vietnl.sharedlibrary.core.services;

/**
 * Giao diện chuẩn hóa cho thao tác CRUD, có thể tái sử dụng cho nhiều entity khác nhau.
 *
 * @param <E> Entity class (VD: User, Product)
 * @param <ID> Kiểu ID (VD: Long, UUID)
 * @param <REQ> Request DTO từ client (VD: UserCreateRequest)
 */
public interface ICrudService<E, ID, RES, REQ>
    extends ICreateService<E, ID, RES, REQ>,
        IUpdateService<E, ID, RES, REQ>,
        IPatchService<E, ID, RES, REQ>,
        IGetService<E, ID, RES>,
        IDeleteService<E, ID> {}
