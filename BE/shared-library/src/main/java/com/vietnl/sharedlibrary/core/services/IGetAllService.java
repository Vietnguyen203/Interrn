package com.vietnl.sharedlibrary.core.services;

import com.eps.shared.core.constants.ParamsKeys;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.persistence.IJpaGetAllPersistence;
import com.eps.shared.core.services.v2.GetAllService;
import com.eps.shared.core.utils.*;
import com.eps.shared.core.utils.DateTimeUtils;
import com.eps.shared.core.utils.functions.PentaFunction;
import com.eps.shared.core.valueobject.PositionType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;

public interface IGetAllService<E, RES> extends GetAllService<RES> {

  IJpaGetAllPersistence<E> getJpaGetAllPersistence();

  public List<Class<?>> supportedTypes =
      List.of(
          boolean.class,
          Boolean.class,
          String.class,
          Integer.class,
          int.class,
          long.class,
          Long.class,
          UUID.class,
          float.class,
          Float.class,
          double.class,
          Double.class,
          Timestamp.class,
          LocalDateTime.class,
          ZonedDateTime.class,
          Instant.class,
          Enum.class);

  List<Class<?>> supportedTimeTimes =
      List.of(Timestamp.class, LocalDateTime.class, ZonedDateTime.class, Instant.class);

  @SuppressWarnings("unchecked")
  default Page<RES> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      Map<String, Object> filter,
      PentaFunction<
              HeaderContext,
              Root<E>,
              CriteriaQuery<?>,
              CriteriaBuilder,
              Map<String, Object>,
              List<Predicate>>
          buildFilterResolver,
      BiFunction<HeaderContext, Page<E>, Page<RES>> mappingPageResponseHandler) {

    Pageable pageable = PageableUtils.convertPageable(page, pageSize, sort);

    Specification<E> query = buildQuery(context, search, filter, buildFilterResolver);
    Page<E> data = getJpaGetAllPersistence().findAll(query, pageable);
    return mappingPageResponseHandler.apply(context, data);
    //    return data.map(item -> mappingResponseHandler.apply(context, item));
  }

  @Override
  default Page<RES> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      Map<String, Object> filter) {

    return getAll(
        context,
        search,
        page,
        pageSize,
        sort,
        filter,
        this::buildFilterQuery,
        this::mappingPageResponse);
  }

  default Page<RES> mappingPageResponse(HeaderContext context, Page<E> items) {

    return items.map(
        item -> {
          RES resItem =
              com.eps.shared.core.utils.GenericTypeUtils.getNewInstance(
                  this, IGetAllService.class, PositionType.LAST);

          FnCommon.copyProperties(resItem, item);
          return resItem;
        });
  }

  default Specification<E> buildQuery(
      HeaderContext context,
      String search,
      Map<String, Object> filter,
      PentaFunction<
              HeaderContext,
              Root<E>,
              CriteriaQuery<?>,
              CriteriaBuilder,
              Map<String, Object>,
              List<Predicate>>
          buildFilterResolver) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      List<Predicate> entityPredicates =
          buildEntityQuery(root.getModel().getAttributes(), root, query, cb, filter);

      //      List<Predicate> filterPredicates = buildFilterQuery(context, root, query, cb, filter);
      List<Predicate> filterPredicates =
          buildFilterResolver.apply(context, root, query, cb, filter);

      if (StringUtils.hasLength(search)) {
        Predicate searchPredicate =
            buildSearchQuery(root.getModel().getAttributes(), root, query, cb, search);
        predicates.add(searchPredicate);
      }
      predicates.addAll(entityPredicates);
      predicates.addAll(filterPredicates);
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  default List<Predicate> buildFilterQuery(
      HeaderContext context,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Object> filter) {

    return new ArrayList<>();
  }

  default List<Predicate> buildEntityQuery(
      Set<Attribute<? super E, ?>> fieldNames,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Object> filter) {

    List<Predicate> predicates = new ArrayList<>();

    for (Attribute<? super E, ?> attribute : fieldNames) {
      String fieldName = attribute.getName();
      Object value = filter.get(fieldName);

      if (value == null) {
        continue;
      }

      Class<?> fieldType = attribute.getJavaType();

      if (fieldType.isEnum() && value instanceof String) {
        Object enumValue = com.eps.shared.core.utils.EnumUtils.convertEnum(fieldType, value);
        predicates.add(cb.equal(root.get(fieldName), enumValue));
        continue;
      }

      if (fieldType.getTypeName().equals(UUID.class.getName()) && value instanceof String) {
        Object uuid = UUID.fromString((String) value);
        predicates.add(cb.equal(root.get(fieldName), uuid));
        continue;
      }

      if (!supportedTypes.contains(fieldType)) {
        continue;
      }

      if (supportedTimeTimes.contains(fieldType)) {
        String fromKey = ParamsKeys.getFieldName(ParamsKeys.PREFIX_FROM, fieldName);
        String toKey = ParamsKeys.getFieldName(ParamsKeys.PREFIX_TO, fieldName);

        Object fromValue = filter.get(fromKey);
        Object toValue = filter.get(toKey);

        if (fromValue != null) {
          predicates.add(
              cb.greaterThanOrEqualTo(
                  root.get(fieldName), DateTimeUtils.toLocalDateTime((String) fromValue)));
        }

        if (toValue != null) {
          predicates.add(
              cb.lessThanOrEqualTo(
                  root.get(fieldName), DateTimeUtils.toLocalDateTime((String) toValue)));
        }

      } else {
        predicates.add(cb.equal(root.get(fieldName), value));
      }
    }

    return predicates;
  }

  default String[] getSearchFieldNames() {

    return new String[] {"name", "ten", "ma", "code"};
  }

  default Predicate buildSearchQuery(
      Set<Attribute<? super E, ?>> fieldNames,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      String search) {
    String[] searchFieldNames = getSearchFieldNames();
    List<Predicate> searchPredicates = new ArrayList<>();
    List<String> entityFieldNames = fieldNames.stream().map(Attribute::getName).toList();
    for (String fieldName : searchFieldNames) {
      boolean isSearch = entityFieldNames.contains(fieldName);
      if (isSearch && StringUtils.hasLength(search)) {
        Predicate searchPredicate =
            cb.like(cb.lower(root.get(fieldName)), "%" + search.toLowerCase() + "%");

        searchPredicates.add(searchPredicate);
      }
    }

    return cb.or(searchPredicates.toArray(new Predicate[0]));
  }
}
