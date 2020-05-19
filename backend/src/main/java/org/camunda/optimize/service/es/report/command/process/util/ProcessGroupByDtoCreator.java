/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.report.command.process.util;

import org.camunda.optimize.dto.optimize.query.report.single.group.GroupByDateUnit;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.AssigneeGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.CandidateGroupGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.EndDateGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.FlowNodesGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.NoneGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.ProcessGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.RunningDateGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.StartDateGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.UserTasksGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.VariableGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.value.DateGroupByValueDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.value.VariableGroupByValueDto;
import org.camunda.optimize.dto.optimize.query.variable.VariableType;


public class ProcessGroupByDtoCreator {

  public static ProcessGroupByDto<?> createGroupByStartDateDto(GroupByDateUnit dateInterval) {
    StartDateGroupByDto groupByDto = new StartDateGroupByDto();
    DateGroupByValueDto valueDto = new DateGroupByValueDto();
    valueDto.setUnit(dateInterval);
    groupByDto.setValue(valueDto);

    return groupByDto;
  }

  public static ProcessGroupByDto<?> createGroupByStartDateDto() {
    return createGroupByStartDateDto(null);
  }

  public static ProcessGroupByDto<?> createGroupByEndDateDto(GroupByDateUnit dateInterval) {
    EndDateGroupByDto groupByDto = new EndDateGroupByDto();
    DateGroupByValueDto valueDto = new DateGroupByValueDto();
    valueDto.setUnit(dateInterval);
    groupByDto.setValue(valueDto);

    return groupByDto;
  }

  public static ProcessGroupByDto<?> createGroupByEndDateDto() {
    return createGroupByEndDateDto(null);
  }

  public static ProcessGroupByDto<?> createGroupByRunningDateDto(GroupByDateUnit dateInterval) {
    RunningDateGroupByDto groupByDto = new RunningDateGroupByDto();
    DateGroupByValueDto valueDto = new DateGroupByValueDto();
    valueDto.setUnit(dateInterval);
    groupByDto.setValue(valueDto);

    return groupByDto;
  }

  public static ProcessGroupByDto<?> createGroupByRunningDateDto() {
    return createGroupByRunningDateDto(null);
  }

  public static ProcessGroupByDto<?> createGroupByFlowNode() {
    return new FlowNodesGroupByDto();
  }

  public static ProcessGroupByDto<?> createGroupByUserTasks() {
    return new UserTasksGroupByDto();
  }

  public static ProcessGroupByDto<?> createGroupByAssignee() {
    return new AssigneeGroupByDto();
  }

  public static ProcessGroupByDto<?> createGroupByCandidateGroup() {
    return new CandidateGroupGroupByDto();
  }

  public static ProcessGroupByDto<?> createGroupByNone() {
    return new NoneGroupByDto();
  }

  public static ProcessGroupByDto<?> createGroupByVariable(String variableName, VariableType variableType) {
    VariableGroupByValueDto groupByValueDto = new VariableGroupByValueDto();
    groupByValueDto.setName(variableName);
    groupByValueDto.setType(variableType);
    VariableGroupByDto groupByDto = new VariableGroupByDto();
    groupByDto.setValue(groupByValueDto);
    return groupByDto;
  }

  public static ProcessGroupByDto<?> createGroupByVariable() {
    return createGroupByVariable(null, null);
  }
}
