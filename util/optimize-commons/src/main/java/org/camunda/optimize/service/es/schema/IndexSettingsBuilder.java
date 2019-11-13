/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.schema;

import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class IndexSettingsBuilder {

  public static final int MAX_GRAM = 10;
  public static final String DYNAMIC_SETTING_MAX_NGRAM_DIFF = "max_ngram_diff";

  public static Settings buildDynamicSettings(ConfigurationService configurationService) throws IOException {
    XContentBuilder builder = jsonBuilder();
    // @formatter:off
    builder
      .startObject();
        addDynamicSettings(configurationService, builder)
      .endObject();
    // @formatter:on
    return toSettings(builder);
  }

  public static Settings buildAllSettings(ConfigurationService configurationService) throws IOException {
    XContentBuilder builder = jsonBuilder();
    // @formatter:off
    builder
      .startObject();
        addDynamicSettings(configurationService, builder);
        addStaticSettings(configurationService, builder);
        addAnalysis(builder)
      .endObject();
    // @formatter:on
    return toSettings(builder);
  }

  private static XContentBuilder addStaticSettings(final ConfigurationService configurationService,
                                                   final XContentBuilder builder) throws IOException {
    return builder
      .field("number_of_shards", configurationService.getEsNumberOfShards());
  }

  private static XContentBuilder addDynamicSettings(final ConfigurationService configurationService,
                                                    final XContentBuilder builder) throws IOException {
    return builder
      .field(DYNAMIC_SETTING_MAX_NGRAM_DIFF, MAX_GRAM - 1)
      .field("refresh_interval", configurationService.getEsRefreshInterval())
      .field("number_of_replicas", configurationService.getEsNumberOfReplicas());
  }

  private static XContentBuilder addAnalysis(XContentBuilder builder) throws IOException {
    // @formatter:off
    return builder
    .startObject("analysis")
      .startObject("analyzer")
        .startObject("lowercase_ngram")
          .field("type", "custom")
          .field("tokenizer", "ngram_tokenizer")
          .field("filter", "lowercase")
        .endObject()
        // this analyzer is supposed to be used for large text fields for which we only want to
        // query for whether they are empty or not, e.g. the xml of definitions
        // see https://app.camunda.com/jira/browse/OPT-2911
        .startObject("is_present_analyzer")
          .field("type", "custom")
          .field("tokenizer", "keyword")
          .field("filter", "is_present_filter")
        .endObject()
      .endObject()
      .startObject("normalizer")
        .startObject("lowercase_normalizer")
          .field("type", "custom")
          .field("filter", new String[]{"lowercase"})
        .endObject()
      .endObject()
      .startObject("tokenizer")
        .startObject("ngram_tokenizer")
          .field("type", "nGram")
          .field("min_gram", 1)
          .field("max_gram", MAX_GRAM)
        .endObject()
      .endObject()
      .startObject("filter")
        .startObject("is_present_filter")
          .field("type", "truncate")
          .field("length", "1")
        .endObject()
      .endObject()
    .endObject();
    // @formatter:on
  }

  public static Settings toSettings(final XContentBuilder builder) {
    return Settings.builder().loadFromSource(Strings.toString(builder), XContentType.JSON).build();
  }
}
