/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.clients;

import io.camunda.search.clients.auth.AuthorizationQueryStrategy;
import io.camunda.search.clients.auth.DocumentAuthorizationQueryStrategy;
import io.camunda.search.clients.query.SearchQuery;
import io.camunda.search.clients.transformers.ServiceTransformers;
import io.camunda.search.clients.transformers.filter.AuthenticationTransformer;
import io.camunda.search.clients.transformers.query.SearchQueryResultTransformer;
import io.camunda.search.clients.transformers.query.TypedSearchQueryTransformer;
import io.camunda.search.exception.SearchQueryExecutionException;
import io.camunda.search.filter.FilterBase;
import io.camunda.search.query.SearchQueryResult;
import io.camunda.search.query.TypedSearchQuery;
import io.camunda.search.sort.SortOption;
import io.camunda.security.auth.SecurityContext;

public final class SearchClientBasedQueryExecutor {

  private final DocumentBasedSearchClient searchClient;
  private final ServiceTransformers transformers;
  private final SecurityContext securityContext;
  private final AuthorizationQueryStrategy authorizationQueryStrategy;

  public SearchClientBasedQueryExecutor(
      final DocumentBasedSearchClient searchClient,
      final ServiceTransformers transformers,
      final SecurityContext securityContext) {
    this.searchClient = searchClient;
    this.transformers = transformers;
    this.securityContext = securityContext;
    authorizationQueryStrategy = new DocumentAuthorizationQueryStrategy(searchClient, transformers);
  }

  public <T extends FilterBase, S extends SortOption, R> SearchQueryResult<R> search(
      final TypedSearchQuery<T, S> query, final Class<R> documentClass) {
    final var authenticationCheck = getAuthenticationCheckIfPresent();
    final var transformer = getSearchQueryRequestTransformer(query);
    final var searchRequest = transformer.applyWithAuthentication(query, authenticationCheck);
    final var authorizedSearchRequest =
        authorizationQueryStrategy.applyAuthorizationToQuery(
            searchRequest, securityContext, query.getClass());
    final SearchQueryResultTransformer<R> responseTransformer = getSearchResultTransformer();
    return responseTransformer.apply(searchClient.search(authorizedSearchRequest, documentClass));
  }

  private SearchQuery getAuthenticationCheckIfPresent() {
    if (securityContext.authentication() != null) {
      return AuthenticationTransformer.INSTANCE.toSearchQuery(securityContext.authentication());
    }
    return null;
  }

  private <T extends FilterBase, S extends SortOption>
      TypedSearchQueryTransformer<T, S> getSearchQueryRequestTransformer(
          final TypedSearchQuery<T, S> query) {
    return transformers.getTypedSearchQueryTransformer(query.getClass());
  }

  private <R> SearchQueryResultTransformer<R> getSearchResultTransformer() {
    return new SearchQueryResultTransformer<R>();
  }

  private SearchQueryExecutionException rethrowRuntimeException(final Exception e) {
    return new SearchQueryExecutionException("Failed to execute search query", e);
  }
}
