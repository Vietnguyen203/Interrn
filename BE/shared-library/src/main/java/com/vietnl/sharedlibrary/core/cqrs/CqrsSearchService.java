package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.aware.PageResultAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.query.SearchQuery;
import com.eps.shared.core.services.v2.GetAllService;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface CqrsSearchService<E, R>
    extends GetAllService<R>, MediatorAware, EntityAware<E>, PageResultAware<R> {

  @Override
  default Page<R> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      Map<String, Object> filter) {
    return getMediator()
        .query(
            context,
            new SearchQuery<>(
                getEntityClass(), search, page, pageSize, sort, filter, getPageResultClass()));
  }
}
