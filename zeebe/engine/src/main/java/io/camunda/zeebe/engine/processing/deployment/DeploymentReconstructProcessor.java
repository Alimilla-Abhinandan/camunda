/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.deployment;

import io.camunda.zeebe.engine.processing.ExcludeAuthorizationCheck;
import io.camunda.zeebe.engine.processing.deployment.DeploymentReconstructProcessor.Resource.ProcessResource;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.StateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.deployment.PersistedProcess;
import io.camunda.zeebe.engine.state.immutable.DecisionState;
import io.camunda.zeebe.engine.state.immutable.DeploymentState;
import io.camunda.zeebe.engine.state.immutable.FormState;
import io.camunda.zeebe.engine.state.immutable.ProcessState;
import io.camunda.zeebe.engine.state.immutable.ProcessingState;
import io.camunda.zeebe.protocol.impl.record.value.deployment.DeploymentRecord;
import io.camunda.zeebe.protocol.record.intent.DeploymentIntent;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;
import io.camunda.zeebe.util.buffer.BufferUtil;
import java.util.HashSet;
import java.util.Set;
import org.agrona.collections.MutableReference;

@ExcludeAuthorizationCheck
public class DeploymentReconstructProcessor implements TypedRecordProcessor<DeploymentRecord> {
  private static final long NO_DEPLOYMENT_KEY = -1;
  private final ChecksumGenerator checksumGenerator = new ChecksumGenerator();
  private final KeyGenerator keyGenerator;
  private final DeploymentState deploymentState;
  private final ProcessState processState;
  private final FormState formState;
  private final DecisionState decisionState;
  private final StateWriter stateWriter;

  public DeploymentReconstructProcessor(
      final KeyGenerator keyGenerator,
      final ProcessingState processingState,
      final Writers writers) {
    this.keyGenerator = keyGenerator;
    deploymentState = processingState.getDeploymentState();
    processState = processingState.getProcessState();
    formState = processingState.getFormState();
    decisionState = processingState.getDecisionState();
    stateWriter = writers.state();
  }

  @Override
  public void processRecord(final TypedRecord<DeploymentRecord> record) {
    final var key = keyGenerator.nextKey();
    final var resource = findNextResource();
    if (resource == null) {
      stateWriter.appendFollowUpEvent(key, DeploymentIntent.RECONSTRUCTED_ALL, record.getValue());
      return;
    }

    final DeploymentRecord deploymentRecord;
    if (resource.deploymentKey() != NO_DEPLOYMENT_KEY) {
      final var allResourcesOfDeployment = findResourcesWithDeploymentKey(resource.deploymentKey());
      deploymentRecord =
          recreateDeploymentForResources(
              resource.deploymentKey(), resource.tenantId(), allResourcesOfDeployment);
    } else {
      deploymentRecord = createNewDeploymentForResource(resource);
    }
    stateWriter.appendFollowUpEvent(
        deploymentRecord.getDeploymentKey(), DeploymentIntent.RECONSTRUCTED, deploymentRecord);
  }

  private Resource findNextResource() {
    final var foundProcess = new MutableReference<PersistedProcess>();
    processState.forEachProcess(
        null, // TODO: Continue where we left off
        process -> {
          final var deploymentKey = process.getDeploymentKey();
          if (deploymentKey != NO_DEPLOYMENT_KEY
              && deploymentState.hasStoredDeploymentRecord(deploymentKey)) {
            return true;
          }
          final var copy = new PersistedProcess();
          BufferUtil.copy(process, copy);
          foundProcess.set(copy);
          return false;
        });

    if (foundProcess.get() != null) {
      return new ProcessResource(foundProcess.get());
    }
    // TODO: Continue with formState and decisionState

    return null;
  }

  private Set<Resource> findResourcesWithDeploymentKey(final long deploymentKey) {
    // Iterate through processState, formState, and decisionState to find resources that are marked
    // with the given deployment key.
    final var resources = new HashSet<Resource>();
    processState.forEachProcess(
        null,
        process -> {
          final var processDeploymentKey = process.getDeploymentKey();
          if (processDeploymentKey == deploymentKey) {
            final var copy = new PersistedProcess();
            BufferUtil.copy(process, copy);
            resources.add(new ProcessResource(copy));
          }
          return true;
        });
    // TODO: Continue with formState and decisionState
    return resources;
  }

  /**
   * Creates a new deployment for a given resource that was not marked with a deployment key. We
   * reuse the resource key as new deployment key.
   */
  private DeploymentRecord createNewDeploymentForResource(final Resource resource) {
    final var deploymentRecord = new DeploymentRecord();
    switch (resource) {
      case ProcessResource(final var process) -> {
        deploymentRecord.setDeploymentKey(process.getKey());
        deploymentRecord.setTenantId(process.getTenantId());
        attachResourceMetadataToDeployment(deploymentRecord, resource);
      }
    }
    return deploymentRecord;
  }

  /**
   * Recreates a deployment for all resources that are already marked with a given deployment key.
   */
  private DeploymentRecord recreateDeploymentForResources(
      final long deploymentKey, final String tenantId, final Set<Resource> resources) {
    final var deploymentRecord = new DeploymentRecord();
    deploymentRecord.setDeploymentKey(deploymentKey);
    deploymentRecord.setTenantId(tenantId);
    for (final var resource : resources) {
      attachResourceMetadataToDeployment(deploymentRecord, resource);
    }
    return deploymentRecord;
  }

  private void attachResourceMetadataToDeployment(
      final DeploymentRecord deploymentRecord, final Resource resource) {
    switch (resource) {
      case ProcessResource(final var process) -> {
        deploymentRecord
            .processesMetadata()
            .add()
            .setBpmnProcessId(process.getBpmnProcessId())
            .setVersion(process.getVersion())
            .setKey(process.getKey())
            .setResourceName(process.getResourceName())
            .setChecksum(checksumGenerator.checksum(process.getResource()))
            .setTenantId(process.getTenantId());
      }
    }
  }

  sealed interface Resource {
    long deploymentKey();

    String tenantId();

    record ProcessResource(PersistedProcess process) implements Resource {
      @Override
      public long deploymentKey() {
        return process.getDeploymentKey();
      }

      @Override
      public String tenantId() {
        return process.getTenantId();
      }
    }
  }
}
