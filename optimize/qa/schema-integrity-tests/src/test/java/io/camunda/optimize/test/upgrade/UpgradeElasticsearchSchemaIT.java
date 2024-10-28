/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.test.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.optimize.service.util.configuration.DatabaseType;
import io.camunda.optimize.test.upgrade.client.ElasticsearchSchemaTestClient;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.client.indices.IndexTemplateMetadata;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeElasticsearchSchemaIT
    extends AbstractDatabaseSchemaIT<ElasticsearchSchemaTestClient> {
  private static final Logger LOG = LoggerFactory.getLogger(UpgradeElasticsearchSchemaIT.class);

  Map<String, Map> expectedSettings;
  Map<String, MappingMetadata> expectedMappings;
  Map<String, Set<AliasMetadata>> expectedAliases;
  List<IndexTemplateMetadata> expectedTemplates;

  @Override
  protected String getOptimizeUpdateLogPath() {
    return getBuildDirectory() + "/es-update-schema-optimize-upgrade.log";
  }

  @Override
  protected String getNewOptimizeOutputLogPath() {
    return getBuildDirectory() + "/es-update-schema-new-optimize-startup.log";
  }

  @Override
  protected String getOldOptimizeOutputLogPath() {
    return getBuildDirectory() + "/es-update-schema-old-optimize-startup.log";
  }

  @Override
  protected void assertMigratedDatabaseIndicesMatchExpected() throws IOException {
    LOG.info(
        "Expected settings size: {}, keys: {}", expectedSettings.size(), expectedSettings.keySet());
    final Map<String, Map> newSettings = newDatabaseSchemaClient.getSettings();
    LOG.info("Actual settings size: {}, keys: {}", newSettings.size(), newSettings.keySet());
    assertThat(newSettings).isEqualTo(expectedSettings);
    assertThat(newDatabaseSchemaClient.getMappings()).isEqualTo(expectedMappings);
  }

  @Override
  protected void assertMigratedDatabaseAliasesMatchExpected() throws IOException {
    LOG.info(
        "Expected aliases size: {}, keys: {}", expectedAliases.size(), expectedAliases.keySet());
    final Map<String, Set<AliasMetadata>> newAliases = newDatabaseSchemaClient.getAliases();
    LOG.info("Actual aliases size: {}, keys: {}", newAliases.size(), newAliases.keySet());
    assertThat(newAliases).isEqualTo(expectedAliases);
  }

  @Override
  protected void assertMigratedDatabaseTemplatesMatchExpected() throws IOException {
    LOG.info(
        "Expected templates size: {}, names: {}",
        expectedTemplates.size(),
        expectedTemplates.stream().map(IndexTemplateMetadata::name).toList());
    final List<IndexTemplateMetadata> newTemplates = newDatabaseSchemaClient.getTemplates();
    LOG.info(
        "Actual templates size: {}, names: {}",
        newTemplates.size(),
        newTemplates.stream().map(IndexTemplateMetadata::name).toList());
    assertThat(newTemplates).containsExactlyInAnyOrderElementsOf(expectedTemplates);
  }

  @Override
  protected void saveNewOptimizeDatabaseStatus() throws IOException {
    expectedSettings = newDatabaseSchemaClient.getSettings();
    expectedMappings = newDatabaseSchemaClient.getMappings();
    expectedAliases = newDatabaseSchemaClient.getAliases();
    expectedTemplates = newDatabaseSchemaClient.getTemplates();
  }

  @Override
  protected void initializeClientAndCleanDatabase() throws IOException {
    oldDatabaseSchemaClient = new ElasticsearchSchemaTestClient("old", getOldDatabasePort());
    oldDatabaseSchemaClient.cleanIndicesAndTemplates();
    newDatabaseSchemaClient = new ElasticsearchSchemaTestClient("new", getNewDatabasePort());
    newDatabaseSchemaClient.cleanIndicesAndTemplates();
  }

  @Override
  protected DatabaseType getDatabaseType() {
    return DatabaseType.ELASTICSEARCH;
  }
}
