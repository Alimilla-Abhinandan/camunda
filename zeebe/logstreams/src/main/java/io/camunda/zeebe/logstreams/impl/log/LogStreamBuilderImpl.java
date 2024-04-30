/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.logstreams.impl.log;

import com.netflix.concurrency.limits.Limit;
import io.camunda.zeebe.logstreams.impl.LogStreamMetrics;
import io.camunda.zeebe.logstreams.log.LogStream;
import io.camunda.zeebe.logstreams.log.LogStreamBuilder;
import io.camunda.zeebe.logstreams.storage.LogStorage;
import io.camunda.zeebe.scheduler.ActorSchedulingService;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import io.camunda.zeebe.scheduler.future.CompletableActorFuture;
import java.util.Objects;

public final class LogStreamBuilderImpl implements LogStreamBuilder {
  private static final int MINIMUM_FRAGMENT_SIZE = 4 * 1024;
  private int maxFragmentSize = 1024 * 1024 * 4;
  private int partitionId = -1;
  private ActorSchedulingService actorSchedulingService;
  private LogStorage logStorage;
  private String logName;
  private Limit appendLimit;

  @Override
  public LogStreamBuilder withActorSchedulingService(
      final ActorSchedulingService actorSchedulingService) {
    this.actorSchedulingService = actorSchedulingService;
    return this;
  }

  @Override
  public LogStreamBuilder withMaxFragmentSize(final int maxFragmentSize) {
    this.maxFragmentSize = maxFragmentSize;
    return this;
  }

  @Override
  public LogStreamBuilder withLogStorage(final LogStorage logStorage) {
    this.logStorage = logStorage;
    return this;
  }

  @Override
  public LogStreamBuilder withPartitionId(final int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  @Override
  public LogStreamBuilder withLogName(final String logName) {
    this.logName = logName;
    return this;
  }

  @Override
  public LogStreamBuilder withAppendLimit(final Limit appendLimit) {
    this.appendLimit = appendLimit;
    return this;
  }

  @Override
  public ActorFuture<LogStream> buildAsync() {
    validate();

    final var logStreamMetrics = new LogStreamMetrics(partitionId);
    final var logStreamService =
        new LogStreamImpl(
            logName, partitionId, maxFragmentSize, logStorage, appendLimit, logStreamMetrics);

    final var logstreamInstallFuture = new CompletableActorFuture<LogStream>();
    actorSchedulingService
        .submitActor(logStreamService)
        .onComplete(
            (v, t) -> {
              if (t == null) {
                logstreamInstallFuture.complete(logStreamService);
              } else {
                logstreamInstallFuture.completeExceptionally(t);
              }
            });

    return logstreamInstallFuture;
  }

  private void validate() {
    Objects.requireNonNull(actorSchedulingService, "Must specify a actor scheduler");
    Objects.requireNonNull(logStorage, "Must specify a log storage");
    Objects.requireNonNull(appendLimit, "Must specify a append limit");

    if (maxFragmentSize < MINIMUM_FRAGMENT_SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "Expected fragment size to be at least '%d', but was '%d'",
              MINIMUM_FRAGMENT_SIZE, maxFragmentSize));
    }
  }
}
