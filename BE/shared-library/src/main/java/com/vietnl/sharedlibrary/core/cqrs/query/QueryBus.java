package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.exception.QueryHandlerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class QueryBus {

  private final List<QueryMiddleware> middlewares;
  private final Map<String, QueryHandler<?, ?>> handlerCache;

  public QueryBus(ApplicationContext applicationContext, List<QueryMiddleware> middlewares) {
    this.middlewares = new ArrayList<>(middlewares);
    AnnotationAwareOrderComparator.sort(this.middlewares);
    this.handlerCache = buildHandlerCache(applicationContext);
    log.info(
        "[CQRS] MVC Initialized with {} query handlers, {} query middlewares",
        handlerCache.size(),
        this.middlewares.size());
  }

  @SuppressWarnings("unchecked")
  public <R> R dispatch(HeaderContext context, Query<R> query) {
    QueryHandler<?, ?> handler = handlerCache.get(query.getResolvableType().toString());

    if (handler == null) {
      throw new QueryHandlerNotFoundException(
          "No handler found for query: " + query.getClass().getSimpleName());
    }

    QueryHandlerDelegate<R> pipeline = buildPipeline((QueryHandler<Query<R>, R>) handler);
    return pipeline.handle(context, query);
  }

  private <R> QueryHandlerDelegate<R> buildPipeline(QueryHandler<Query<R>, R> handler) {
    QueryHandlerDelegate<R> chain = handler::handle;

    for (int i = middlewares.size() - 1; i >= 0; i--) {
      final QueryMiddleware middleware = middlewares.get(i);
      final QueryHandlerDelegate<R> next = chain;
      chain = (ctx, qry) -> middleware.handle(ctx, qry, next);
    }

    return chain;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, QueryHandler<?, ?>> buildHandlerCache(ApplicationContext ctx) {
    Map<String, QueryHandler<?, ?>> cache = new ConcurrentHashMap<>();

    String[] beanNames = ctx.getBeanNamesForType(QueryHandler.class);
    for (String beanName : beanNames) {
      QueryHandler<?, ?> handler = (QueryHandler<?, ?>) ctx.getBean(beanName);

      ResolvableType queryType =
          ResolvableType.forClass(handler.getClass())
              .as(QueryHandler.class)
              .getGeneric(0);

      if (cache.containsKey(queryType.toString())) {
        log.warn(
            "Duplicate QueryHandler for type: {} at bean: {}. Overwriting...",
            queryType,
            beanName);
      }

      cache.put(queryType.toString(), handler);
    }

    return cache;
  }
}
