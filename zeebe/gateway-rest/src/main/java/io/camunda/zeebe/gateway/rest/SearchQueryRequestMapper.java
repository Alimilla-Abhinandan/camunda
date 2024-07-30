/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest;

import io.camunda.service.search.filter.DecisionDefinitionFilter;
import io.camunda.service.search.filter.FilterBase;
import io.camunda.service.search.filter.FilterBuilders;
import io.camunda.service.search.filter.ProcessInstanceFilter;
import io.camunda.service.search.filter.VariableValueFilter;
import io.camunda.service.search.page.SearchQueryPage;
import io.camunda.service.search.query.DecisionDefinitionQuery;
import io.camunda.service.search.query.ProcessInstanceQuery;
import io.camunda.service.search.query.SearchQueryBuilders;
import io.camunda.service.search.query.TypedSearchQueryBuilder;
import io.camunda.service.search.sort.DecisionDefinitionSort;
import io.camunda.service.search.sort.ProcessInstanceSort;
import io.camunda.service.search.sort.SortOption;
import io.camunda.service.search.sort.SortOptionBuilders;
import io.camunda.util.ObjectBuilder;
import io.camunda.zeebe.gateway.protocol.rest.DecisionDefinitionFilterRequest;
import io.camunda.zeebe.gateway.protocol.rest.DecisionDefinitionSearchQueryRequest;
import io.camunda.zeebe.gateway.protocol.rest.ProcessInstanceFilterRequest;
import io.camunda.zeebe.gateway.protocol.rest.ProcessInstanceSearchQueryRequest;
import io.camunda.zeebe.gateway.protocol.rest.SearchQueryPageRequest;
import io.camunda.zeebe.gateway.protocol.rest.SearchQuerySortRequest;
import io.camunda.zeebe.gateway.protocol.rest.VariableValueFilterRequest;
import io.camunda.zeebe.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class SearchQueryRequestMapper {

  private SearchQueryRequestMapper() {}

  public static Either<ProblemDetail, ProcessInstanceQuery> toProcessInstanceQuery(
      final ProcessInstanceSearchQueryRequest request) {
    if (request == null) {
      return Either.right(SearchQueryBuilders.processInstanceSearchQuery().build());
    }
    final var page = toSearchQueryPage(request.getPage());
    final var sort =
        toSearchQuerySort(
            request.getSort(),
            SortOptionBuilders::processInstance,
            SearchQueryRequestMapper::applyProcessInstanceSortField);
    final var filter = toProcessInstanceFilter(request.getFilter());
    return buildSearchQuery(filter, sort, page, SearchQueryBuilders::processInstanceSearchQuery);
  }

  public static Either<ProblemDetail, DecisionDefinitionQuery> toDecisionDefinitionQuery(
      final DecisionDefinitionSearchQueryRequest request) {
    if (request == null) {
      return Either.right(SearchQueryBuilders.decisionDefinitionSearchQuery().build());
    }
    final var page = toSearchQueryPage(request.getPage());
    final var sort =
        toSearchQuerySort(
            request.getSort(),
            SortOptionBuilders::decisionDefinition,
            SearchQueryRequestMapper::applyDecisionDefinitionSortField);
    final var filter = toDecisionDefinitionFilter(request.getFilter());
    return buildSearchQuery(filter, sort, page, SearchQueryBuilders::decisionDefinitionSearchQuery);
  }

  private static <
          T,
          B extends TypedSearchQueryBuilder<T, B, F, S>,
          F extends FilterBase,
          S extends SortOption>
      Either<ProblemDetail, T> buildSearchQuery(
          final Either<List<String>, F> processInstanceFilter,
          final Either<List<String>, S> sorting,
          final Either<List<String>, SearchQueryPage> page,
          final Supplier<B> queryBuilderSupplier) {
    final List<String> validationErrors =
        Stream.of(page, sorting, processInstanceFilter)
            .filter(Either::isLeft)
            .flatMap(e -> e.getLeft().stream())
            .toList();
    if (validationErrors.isEmpty()) {
      return Either.right(
          queryBuilderSupplier
              .get()
              .page(page.get())
              .filter(processInstanceFilter.get())
              .sort(sorting.get())
              .build());
    } else {
      return Either.left(
          ProblemDetail.forStatusAndDetail(
              HttpStatus.BAD_REQUEST, String.join(", ", validationErrors)));
    }
  }

  private static Either<List<String>, ProcessInstanceFilter> toProcessInstanceFilter(
      final ProcessInstanceFilterRequest filter) {
    final var builder = FilterBuilders.processInstance();

    if (filter != null) {
      final var variableFilters = toVariableValueFilter(filter.getVariables()).get();
      if (variableFilters != null) {
        builder.variable(variableFilters);
      }
      if (filter.getKey() != null && !filter.getKey().isEmpty()) {
        builder.processInstanceKeys(filter.getKey());
      }
    }

    return Either.right(builder.build());
  }

  private static Either<List<String>, DecisionDefinitionFilter> toDecisionDefinitionFilter(
      final DecisionDefinitionFilterRequest filter) {
    final var builder = FilterBuilders.decisionDefinition();

    if (filter != null) {
      if (filter.getKey() != null) {
        builder.keys(filter.getKey());
      }
      if (filter.getId() != null) {
        builder.ids(filter.getId());
      }
      if (filter.getDecisionId() != null) {
        builder.decisionIds(filter.getDecisionId());
      }
      if (filter.getName() != null) {
        builder.names(filter.getName());
      }
      if (filter.getVersion() != null) {
        builder.versions(filter.getVersion());
      }
      if (filter.getDecisionRequirementsId() != null) {
        builder.decisionRequirementsIds(filter.getDecisionRequirementsId());
      }
      if (filter.getDecisionRequirementsKey() != null) {
        builder.decisionRequirementsKeys(filter.getDecisionRequirementsKey());
      }
      if (filter.getDecisionRequirementsName() != null) {
        builder.decisionRequirementsNames(filter.getDecisionRequirementsName());
      }
      if (filter.getDecisionRequirementsVersion() != null) {
        builder.decisionRequirementsVersions(filter.getDecisionRequirementsVersion());
      }
      if (filter.getTenantId() != null) {
        builder.tenantIds(filter.getTenantId());
      }
    }

    return Either.right(builder.build());
  }

  public static Either<ProblemDetail, List<VariableValueFilter>> toVariableValueFilter(
      final List<VariableValueFilterRequest> filters) {

    final List<VariableValueFilter> result;

    if (filters != null && !filters.isEmpty()) {
      result =
          filters.stream()
              .map(SearchQueryRequestMapper::toVariableValueFilter)
              .map(Either::get)
              .toList();
    } else {
      result = null;
    }

    return Either.right(result);
  }

  private static Either<ProblemDetail, VariableValueFilter> toVariableValueFilter(
      final VariableValueFilterRequest f) {
    return Either.right(
        FilterBuilders.variableValue(
            (v) ->
                v.name(f.getName())
                    .eq(f.getEq())
                    .gt(f.getGt())
                    .gte(f.getGte())
                    .lt(f.getLt())
                    .lte(f.getLte())));
  }

  public static Either<List<String>, SearchQueryPage> toSearchQueryPage(
      final SearchQueryPageRequest requestedPage) {
    if (requestedPage != null) {
      return Either.right(
          SearchQueryPage.of(
              (p) ->
                  p.size(requestedPage.getLimit())
                      .from(requestedPage.getFrom())
                      .searchAfter(toArrayOrNull(requestedPage.getSearchAfter()))
                      .searchBefore(toArrayOrNull(requestedPage.getSearchBefore()))));
    }

    return Either.right(null);
  }

  private static <T, B extends SortOption.AbstractBuilder<B> & ObjectBuilder<T>>
      Either<List<String>, T> toSearchQuerySort(
          final List<SearchQuerySortRequest> sorting,
          final Supplier<B> builderSupplier,
          final BiFunction<String, B, List<String>> sortFieldMapper) {
    if (sorting != null && !sorting.isEmpty()) {
      final List<String> validationErrors = new ArrayList<>();
      final var builder = builderSupplier.get();
      for (SearchQuerySortRequest sort : sorting) {
        validationErrors.addAll(sortFieldMapper.apply(sort.getField(), builder));
        validationErrors.addAll(applySortOrder(sort.getOrder(), builder));
      }

      return validationErrors.isEmpty()
          ? Either.right(builder.build())
          : Either.left(validationErrors);
    }

    return Either.right(null);
  }

  private static List<String> applyProcessInstanceSortField(
      final String field, final ProcessInstanceSort.Builder builder) {
    final List<String> validationErrors = new ArrayList<>();
    if (field == null) {
      validationErrors.add("Sort field must not be null");
    } else {
      switch (field) {
        case "processInstanceKey" -> builder.processInstanceKey();
        case "startDate" -> builder.startDate();
        case "endDate" -> builder.endDate();
        default -> validationErrors.add("Unknown sortBy: " + field);
      }
    }
    return validationErrors;
  }

  private static List<String> applyDecisionDefinitionSortField(
      final String field, final DecisionDefinitionSort.Builder builder) {
    final List<String> validationErrors = new ArrayList<>();
    if (field == null) {
      validationErrors.add("Sort field must not be null");
    } else {
      switch (field) {
        case "id" -> builder.id();
        case "decisionId" -> builder.decisionId();
        case "name" -> builder.name();
        case "version" -> builder.version();
        case "decisionRequirementsId" -> builder.decisionRequirementsId();
        case "decisionRequirementsKey" -> builder.decisionRequirementsKey();
        case "decisionRequirementsName" -> builder.decisionRequirementsName();
        case "decisionRequirementsVersion" -> builder.decisionRequirementsVersion();
        case "tenantId" -> builder.tenantId();
        default -> validationErrors.add("Unknown sortBy: " + field);
      }
    }
    return validationErrors;
  }

  private static List<String> applySortOrder(
      final String order, final SortOption.AbstractBuilder<?> builder) {
    final List<String> validationErrors = new ArrayList<>();
    switch (order.toLowerCase()) {
      case "asc" -> builder.asc();
      case "desc" -> builder.desc();
      default -> validationErrors.add("Unknown sortOrder: " + order);
    }
    return validationErrors;
  }

  private static Object[] toArrayOrNull(final List<Object> values) {
    if (values == null || values.isEmpty()) {
      return null;
    } else {
      return values.toArray();
    }
  }
}
