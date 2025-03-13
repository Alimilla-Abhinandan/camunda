/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.impl;

import io.camunda.zeebe.logstreams.log.LogStreamWriter;
import io.camunda.zeebe.scheduler.ActorSchedulingService;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import io.camunda.zeebe.stream.api.scheduling.AsyncSchedulePool;
import io.camunda.zeebe.stream.api.scheduling.ScheduledCommandCache.StageableScheduledCommandCache;
import io.camunda.zeebe.stream.impl.AsyncUtil.Step;
import io.camunda.zeebe.stream.impl.StreamProcessor.Phase;
import io.camunda.zeebe.stream.impl.metrics.ScheduledTaskMetrics;
import java.time.Duration;
import java.time.InstantSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class AsyncScheduleServiceContext {
  private final ActorSchedulingService actorSchedulingService;
  private final Supplier<Phase> streamProcessorPhaseSupplier;
  private final BooleanSupplier abortCondition;
  private final Supplier<LogStreamWriter> writerSupplier;
  private final StageableScheduledCommandCache commandCache;
  private final InstantSource clock;
  private final Duration interval;
  private final ScheduledTaskMetrics metrics;
  private final int partitionId;

  private final HashMap<AsyncSchedulePool, AsyncProcessingScheduleServiceActor> asyncActors;
  private final HashMap<AsyncSchedulePool, ProcessingScheduleServiceImpl> asyncActorServices;

  public AsyncScheduleServiceContext(
      final ActorSchedulingService actorSchedulingService,
      final Supplier<Phase> streamProcessorPhaseSupplier,
      final BooleanSupplier abortCondition,
      final Supplier<LogStreamWriter> writerSupplier,
      final StageableScheduledCommandCache commandCache,
      final InstantSource clock,
      final Duration interval,
      final ScheduledTaskMetrics metrics,
      final int partitionId) {
    this.actorSchedulingService = actorSchedulingService;
    this.streamProcessorPhaseSupplier = streamProcessorPhaseSupplier;
    this.abortCondition = abortCondition;
    this.writerSupplier = writerSupplier;
    this.commandCache = commandCache;
    this.clock = clock;
    this.interval = interval;
    this.metrics = metrics;
    this.partitionId = partitionId;

    asyncActors = new LinkedHashMap<>();
    asyncActorServices = new LinkedHashMap<>();
  }

  public ProcessingScheduleServiceImpl createActorService() {
    return new ProcessingScheduleServiceImpl(
        streamProcessorPhaseSupplier, // this is volatile
        abortCondition,
        writerSupplier,
        commandCache,
        clock,
        interval,
        metrics);
  }

  public AsyncProcessingScheduleServiceActor getOrCreateAsyncActor(final AsyncSchedulePool pool) {
    return geAsyncActor(pool, true);
  }

  public AsyncProcessingScheduleServiceActor geAsyncActor(
      final AsyncSchedulePool pool, final boolean create) {
    if (!create) {
      return asyncActors.get(pool);
    }
    return asyncActors.computeIfAbsent(pool, this::createAndSubmitAsyncActor);
  }

  public ProcessingScheduleServiceImpl getAsyncActorService(final AsyncSchedulePool pool) {
    return asyncActorServices.get(pool);
  }

  public AsyncProcessingScheduleServiceActor createAsyncActor(final AsyncSchedulePool pool) {
    final var actorService = createActorService();
    final var actor =
        new AsyncProcessingScheduleServiceActor(pool.getName(), actorService, partitionId);

    asyncActorServices.put(pool, actorService);
    asyncActors.put(pool, actor);
    return actor;
  }

  public ActorFuture<Void> submitActor(final AsyncProcessingScheduleServiceActor actor) {
    return actorSchedulingService.submitActor(actor);
  }

  public ActorFuture<Void> closeActorsAsync() {
    final Step[] array =
        asyncActors.values().stream().map(a -> (Step) a::closeAsync).toArray(Step[]::new);
    return AsyncUtil.chainSteps(0, array);
  }

  public void closeActorServices() {
    asyncActorServices.values().forEach(ProcessingScheduleServiceImpl::close);
  }

  private AsyncProcessingScheduleServiceActor createAndSubmitAsyncActor(
      final AsyncSchedulePool pool) {
    final var actor = createAsyncActor(pool);

    submitActor(pool, actor).join();
    return actor;
  }

  private ActorFuture<Void> submitActor(
      final AsyncSchedulePool pool, final AsyncProcessingScheduleServiceActor actor) {
    return actorSchedulingService.submitActor(actor, pool.getSchedulingHints());
  }
}
