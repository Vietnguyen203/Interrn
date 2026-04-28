package com.vietnl.sharedlibrary.data.jpa;

import lombok.RequiredArgsConstructor;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HibernateTracingInterceptor implements Interceptor {

  //  private final TraceService traceService;

  @Override
  public void onInsert(
      Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    Interceptor.super.onInsert(entity, id, state, propertyNames, propertyTypes);
    //    traceService.trace(String.format("Create %s", entity.getClass().getSimpleName()));
  }

  @Override
  public void onUpdate(
      Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    Interceptor.super.onUpdate(entity, id, state, propertyNames, propertyTypes);
    //    traceService.trace(String.format("Update %s", entity.getClass().getSimpleName()));
  }

  @Override
  public void onUpsert(
      Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    Interceptor.super.onUpsert(entity, id, state, propertyNames, propertyTypes);
  }

  @Override
  public void onDelete(Object entity, Object id, String[] propertyNames, Type[] propertyTypes) {
    Interceptor.super.onDelete(entity, id, propertyNames, propertyTypes);
    //    traceService.trace(String.format("Delete %s", entity.getClass().getSimpleName()));
  }
}
