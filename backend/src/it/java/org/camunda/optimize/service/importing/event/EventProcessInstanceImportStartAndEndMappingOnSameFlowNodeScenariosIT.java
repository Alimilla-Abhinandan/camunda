/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.importing.event;

import org.assertj.core.groups.Tuple;
import org.camunda.optimize.dto.optimize.ProcessInstanceDto;
import org.camunda.optimize.dto.optimize.query.event.EventMappingDto;
import org.camunda.optimize.dto.optimize.query.event.SimpleEventDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EventProcessInstanceImportStartAndEndMappingOnSameFlowNodeScenariosIT extends AbstractEventProcessIT {

  public static final String FIRST_EVENT_NAME = "firstEvent";
  public static final String SECOND_EVENT_NAME = "secondEvent";
  public static final String THIRD_EVENT_NAME = "thirdEvent";
  public static final String FOURTH_EVENT_NAME = "fourthEvent";


  public static final OffsetDateTime FIRST_EVENT_DATETIME = OffsetDateTime.parse("2019-12-12T12:00:00.000+01:00");
  public static final OffsetDateTime SECOND_EVENT_DATETIME = OffsetDateTime.parse("2019-12-12T12:00:30.000+01:00");
  public static final OffsetDateTime THIRD_EVENT_DATETIME = OffsetDateTime.parse("2019-12-12T12:00:45.000+01:00");
  public static final OffsetDateTime FOURTH_EVENT_DATETIME = OffsetDateTime.parse("2019-12-12T12:01:00.000+01:00");

  private static Stream<Arguments> getScenarios() {
    return Stream.of(
      Arguments.of(
        startMapping(FIRST_EVENT_NAME),
        startAndEndMapping(SECOND_EVENT_NAME, THIRD_EVENT_NAME),
        startMapping(FOURTH_EVENT_NAME)
      ),
      Arguments.of(
        startMapping(FIRST_EVENT_NAME),
        startAndEndMapping(SECOND_EVENT_NAME, THIRD_EVENT_NAME),
        endMapping(FOURTH_EVENT_NAME)
      ),
      Arguments.of(
        endMapping(FIRST_EVENT_NAME),
        startAndEndMapping(SECOND_EVENT_NAME, THIRD_EVENT_NAME),
        endMapping(FOURTH_EVENT_NAME)
      ),
      Arguments.of(
        endMapping(FIRST_EVENT_NAME),
        startAndEndMapping(SECOND_EVENT_NAME, THIRD_EVENT_NAME),
        startMapping(FOURTH_EVENT_NAME)
      )
    );
  }

  @ParameterizedTest
  @MethodSource("getScenarios")
  public void instancesAreGeneratedAsExpectedWhenAllEventsAreInSameBatch(final EventMappingDto startEventMapping,
                                                                         final EventMappingDto intermediateEventMapping,
                                                                         final EventMappingDto endEventMapping) {
    // given
    final String firstEventId = ingestTestEvent(FIRST_EVENT_NAME, FIRST_EVENT_DATETIME);
    final String secondEventId = ingestTestEvent(SECOND_EVENT_NAME, SECOND_EVENT_DATETIME);
    final String thirdEventId = ingestTestEvent(THIRD_EVENT_NAME, THIRD_EVENT_DATETIME);
    final String fourthEventId = ingestTestEvent(FOURTH_EVENT_NAME, FOURTH_EVENT_DATETIME);

    createAndPublishMapping(startEventMapping, intermediateEventMapping, endEventMapping);

    // when
    executeImportCycle();

    // then
    assertProcessInstanceIsAsExpected(
      startEventMapping, intermediateEventMapping, endEventMapping, firstEventId, secondEventId, fourthEventId
    );
  }

  @ParameterizedTest
  @MethodSource("getScenarios")
  public void instancesAreGeneratedAsExpectedWhenEventsAreInDifferentBatches(final EventMappingDto startEventMapping,
                                                                             final EventMappingDto intermediateEventMapping,
                                                                             final EventMappingDto endEventMapping) {
    // given
    embeddedOptimizeExtension.getConfigurationService().getEventImportConfiguration().setMaxPageSize(1);

    final String firstEventId = ingestTestEvent(FIRST_EVENT_NAME, FIRST_EVENT_DATETIME);
    final String secondEventId = ingestTestEvent(SECOND_EVENT_NAME, SECOND_EVENT_DATETIME);
    final String thirdEventId = ingestTestEvent(THIRD_EVENT_NAME, THIRD_EVENT_DATETIME);
    final String fourthEventId = ingestTestEvent(FOURTH_EVENT_NAME, FOURTH_EVENT_DATETIME);

    createAndPublishMapping(startEventMapping, intermediateEventMapping, endEventMapping);

    // when
    executeImportCycle();
    executeImportCycle();
    executeImportCycle();
    executeImportCycle();

    // then
    assertProcessInstanceIsAsExpected(
      startEventMapping, intermediateEventMapping, endEventMapping, firstEventId, secondEventId, fourthEventId
    );
  }

  @ParameterizedTest
  @MethodSource("getScenarios")
  public void instancesAreGeneratedAsExpectedWhenEventsAreInDifferentBatchesAndDifferentIngestionOrder(
    final EventMappingDto startEventMapping,
    final EventMappingDto intermediateEventMapping,
    final EventMappingDto endEventMapping) {
    // given
    embeddedOptimizeExtension.getConfigurationService().getEventImportConfiguration().setMaxPageSize(1);

    final String firstEventId = ingestTestEvent(FIRST_EVENT_NAME, FIRST_EVENT_DATETIME);
    final String secondEventId = ingestTestEvent(SECOND_EVENT_NAME, SECOND_EVENT_DATETIME);
    final String thirdEventId = ingestTestEvent(THIRD_EVENT_NAME, THIRD_EVENT_DATETIME);
    final String fourthEventId = ingestTestEvent(FOURTH_EVENT_NAME, FOURTH_EVENT_DATETIME);

    createAndPublishMapping(startEventMapping, intermediateEventMapping, endEventMapping);

    // when
    executeImportCycle();
    executeImportCycle();
    executeImportCycle();
    executeImportCycle();

    // then
    assertProcessInstanceIsAsExpected(
      startEventMapping, intermediateEventMapping, endEventMapping, firstEventId, secondEventId, fourthEventId
    );
  }

  private void createAndPublishMapping(final EventMappingDto startEventMapping,
                                       final EventMappingDto intermediateEventMapping,
                                       final EventMappingDto endEventMapping) {
    final String eventProcessId = createEventProcessMappingFromEventMappings(
      startEventMapping, intermediateEventMapping, endEventMapping
    );
    eventProcessClient.publishEventProcessMapping(eventProcessId);
  }

  private void assertProcessInstanceIsAsExpected(final EventMappingDto startEventMapping,
                                                 final EventMappingDto intermediateEventMapping,
                                                 final EventMappingDto endEventMapping,
                                                 final String firstEventId,
                                                 final String secondEventId,
                                                 final String fourthEventId) {
    final List<ProcessInstanceDto> processInstances = getEventProcessInstancesFromElasticsearch();
    assertThat(processInstances)
      .hasSize(1)
      .hasOnlyOneElementSatisfying(
        processInstanceDto -> {
          assertThat(processInstanceDto)
            .hasFieldOrPropertyWithValue(ProcessInstanceDto.Fields.state, PROCESS_INSTANCE_STATE_COMPLETED)
            .hasFieldOrPropertyWithValue(
              ProcessInstanceDto.Fields.duration,
              Duration.between(FIRST_EVENT_DATETIME, FOURTH_EVENT_DATETIME).toMillis()
            )
            .hasFieldOrPropertyWithValue(ProcessInstanceDto.Fields.startDate, FIRST_EVENT_DATETIME)
            .hasFieldOrPropertyWithValue(ProcessInstanceDto.Fields.endDate, FOURTH_EVENT_DATETIME)
            .extracting(ProcessInstanceDto::getEvents)
            .satisfies(events -> assertThat(events)
              .allSatisfy(simpleEventDto -> assertThat(simpleEventDto).hasNoNullFieldsOrProperties())
              .extracting(
                SimpleEventDto::getId,
                SimpleEventDto::getActivityId,
                SimpleEventDto::getStartDate,
                SimpleEventDto::getEndDate
              )
              .containsExactlyInAnyOrder(
                Tuple.tuple(
                  firstEventId,
                  BPMN_START_EVENT_ID,
                  FIRST_EVENT_DATETIME,
                  startEventMapping.getEnd() != null ? FIRST_EVENT_DATETIME : SECOND_EVENT_DATETIME
                ),
                Tuple.tuple(
                  secondEventId,
                  BPMN_INTERMEDIATE_EVENT_ID,
                  SECOND_EVENT_DATETIME,
                  THIRD_EVENT_DATETIME
                ),
                Tuple.tuple(
                  fourthEventId,
                  BPMN_END_EVENT_ID,
                  endEventMapping.getStart() != null ? FOURTH_EVENT_DATETIME : THIRD_EVENT_DATETIME,
                  FOURTH_EVENT_DATETIME
                )
              )
            );
        }
      );
  }
}
