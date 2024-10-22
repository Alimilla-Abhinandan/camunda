/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.handlers;

import io.camunda.exporter.store.BatchRequest;
import io.camunda.exporter.utils.ExporterUtil;
import io.camunda.exporter.utils.XMLUtil;
import io.camunda.webapps.schema.entities.operate.ProcessEntity;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.ProcessIntent;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ProcessHandler implements ExportHandler<ProcessEntity, Process> {

  private final String indexName;
  private final XMLUtil xmlUtil;

  public ProcessHandler(final String indexName, final XMLUtil xmlUtil) {
    this.indexName = indexName;
    this.xmlUtil = xmlUtil;
  }

  @Override
  public ValueType getHandledValueType() {
    return ValueType.PROCESS;
  }

  @Override
  public Class<ProcessEntity> getEntityType() {
    return ProcessEntity.class;
  }

  @Override
  public boolean handlesRecord(final Record<Process> record) {
    return record.getIntent().equals(ProcessIntent.CREATED);
  }

  @Override
  public List<String> generateIds(final Record<Process> record) {
    return List.of(String.valueOf(record.getValue().getProcessDefinitionKey()));
  }

  @Override
  public ProcessEntity createNewEntity(final String id) {
    return new ProcessEntity().setId(id);
  }

  @Override
  public void updateEntity(final Record<Process> record, final ProcessEntity entity) {
    final Process process = record.getValue();
    entity
        .setId(String.valueOf(process.getProcessDefinitionKey()))
        .setKey(process.getProcessDefinitionKey())
        .setBpmnProcessId(process.getBpmnProcessId())
        .setVersion(process.getVersion())
        .setTenantId(ExporterUtil.tenantOrDefault(process.getTenantId()));
    final byte[] byteArray = process.getResource();

    final String bpmn = new String(byteArray, StandardCharsets.UTF_8);
    entity.setBpmnXml(bpmn);

    final String resourceName = process.getResourceName();
    entity.setResourceName(resourceName);

    if (!process.getVersionTag().isEmpty()) {
      entity.setVersionTag(process.getVersionTag());
    }

    final Optional<ProcessEntity> diagramData =
        xmlUtil.extractDiagramData(byteArray, process.getBpmnProcessId());
    diagramData.ifPresent(
        processEntity ->
            entity
                .setName(processEntity.getName())
                .setFlowNodes(processEntity.getFlowNodes())
                .setVersionTag(processEntity.getVersionTag())
                .setFormId(processEntity.getFormKey())
                .setIsPublic(processEntity.getIsPublic()));
  }

  @Override
  public void flush(final ProcessEntity entity, final BatchRequest batchRequest) {
    batchRequest.add(indexName, entity);
  }
}
