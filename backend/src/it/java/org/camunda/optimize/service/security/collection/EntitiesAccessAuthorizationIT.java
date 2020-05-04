/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.security.collection;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.optimize.dto.engine.DecisionDefinitionEngineDto;
import org.camunda.optimize.dto.engine.ProcessDefinitionEngineDto;
import org.camunda.optimize.dto.optimize.RoleType;
import org.camunda.optimize.dto.optimize.UserDto;
import org.camunda.optimize.dto.optimize.query.entity.EntityDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.SingleDecisionReportDefinitionDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.SingleProcessReportDefinitionDto;
import org.camunda.optimize.service.security.CaseInsensitiveAuthenticationMockUtil;
import org.camunda.optimize.test.util.decision.DecisionTypeRef;
import org.camunda.optimize.test.util.decision.DmnModelGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.camunda.optimize.test.engine.AuthorizationClient.KERMIT_USER;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class EntitiesAccessAuthorizationIT extends AbstractCollectionRoleIT {

  @ParameterizedTest
  @MethodSource(ACCESS_IDENTITY_ROLES)
  public void containsAuthorizedCollectionsByCollectionUserRole(final IdentityAndRole accessIdentityRolePairs) {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();
    authorizationClient.createKermitGroupAndAddKermitToThatGroup();
    authorizationClient.grantKermitGroupOptimizeAccess();

    final String collectionId = collectionClient.createNewCollectionForAllDefinitionTypes();
    addRoleToCollectionAsDefaultUser(
      accessIdentityRolePairs.roleType, accessIdentityRolePairs.identityDto, collectionId
    );

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(KERMIT_USER, KERMIT_USER);

    // then
    assertThat(authorizedEntities.size(), is(1));
    assertThat(
      authorizedEntities.stream().map(EntityDto::getId).collect(Collectors.toList()),
      containsInAnyOrder(collectionId)
    );
    assertThat(
      authorizedEntities.stream().map(EntityDto::getCurrentUserRole).collect(Collectors.toList()),
      contains(accessIdentityRolePairs.roleType)
    );
  }

  @Test
  public void collectionAccessByRoleDoesNotDependOnUsernameCaseAtLoginWithCaseInsensitiveAuthenticationBackend() {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();
    authorizationClient.createKermitGroupAndAddKermitToThatGroup();
    authorizationClient.grantKermitGroupOptimizeAccess();

    final String allUpperCaseUserId = KERMIT_USER.toUpperCase();
    final String actualUserId = KERMIT_USER;
    final ClientAndServer engineMockServer = useAndGetEngineMockServer();

    final List<HttpRequest> mockedRequests = CaseInsensitiveAuthenticationMockUtil.setupCaseInsensitiveAuthentication(
      embeddedOptimizeExtension, engineIntegrationExtension, engineMockServer,
      allUpperCaseUserId, actualUserId
    );

    final String collectionId = collectionClient.createNewCollectionForAllDefinitionTypes();
    addRoleToCollectionAsDefaultUser(
      RoleType.VIEWER, new UserDto(actualUserId), collectionId
    );

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(allUpperCaseUserId, actualUserId);

    // then
    assertThat(authorizedEntities.size(), is(1));
    assertThat(
      authorizedEntities.stream().map(EntityDto::getId).collect(Collectors.toList()),
      containsInAnyOrder(collectionId)
    );

    mockedRequests.forEach(engineMockServer::verify);
  }

  @Test
  public void superUserAllEntitiesAvailable() {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();
    embeddedOptimizeExtension.getConfigurationService().getSuperUserIds().add(KERMIT_USER);

    final String collectionId = collectionClient.createNewCollectionForAllDefinitionTypes();
    final String combinedReportId = reportClient.createEmptyCombinedReport(null);
    final String processReportId = reportClient.createSingleProcessReport(new SingleProcessReportDefinitionDto());
    final String decisionReportId = reportClient.createSingleDecisionReport(new SingleDecisionReportDefinitionDto());
    final String dashboardId = dashboardClient.createDashboard(null);

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(KERMIT_USER, KERMIT_USER);

    // then
    assertThat(authorizedEntities.size(), is(5));
    assertThat(
      authorizedEntities.stream().map(EntityDto::getId).collect(Collectors.toList()),
      containsInAnyOrder(collectionId, combinedReportId, processReportId, decisionReportId, dashboardId)
    );
    assertThat(
      authorizedEntities.stream().map(EntityDto::getCurrentUserRole).collect(Collectors.toList()),
      everyItem(greaterThanOrEqualTo(RoleType.EDITOR))
    );
  }

  @Test
  public void superUserEntitiesNotAuthorizedForDefinitionAreHidden() {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();
    embeddedOptimizeExtension.getConfigurationService().getSuperUserIds().add(KERMIT_USER);

    ProcessDefinitionEngineDto unauthorizedProcess = deploySimpleServiceTaskProcess("unauthorizedProcess");
    DecisionDefinitionEngineDto unauthorizedDecision = deploySimpleDecisionDefinition("unauthorizedDecision");

    reportClient.createAndStoreProcessReport(unauthorizedProcess.getKey());
    reportClient.createSingleDecisionReportDefinitionDto(unauthorizedDecision.getKey()).getId();

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(KERMIT_USER, KERMIT_USER);

    // then
    assertThat(authorizedEntities.size(), is(0));
  }

  @Test
  public void unauthorizedCollectionAndOtherUsersPrivateItemsNotAvailable() {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();

    collectionClient.createNewCollectionForAllDefinitionTypes();
    reportClient.createEmptyCombinedReport(null);
    reportClient.createSingleProcessReport(new SingleProcessReportDefinitionDto());
    reportClient.createSingleDecisionReport(new SingleDecisionReportDefinitionDto());
    dashboardClient.createDashboard(null);

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(KERMIT_USER, KERMIT_USER);

    // then
    assertThat(authorizedEntities.size(), is(0));
  }

  @Test
  public void privateEntitiesVisibilityDoesNotDependOnUsernameCaseAtLoginWithCaseInsensitiveAuthenticationBackend() {
    // given
    authorizationClient.addKermitUserAndGrantAccessToOptimize();
    authorizationClient.createKermitGroupAndAddKermitToThatGroup();
    authorizationClient.grantKermitGroupOptimizeAccess();

    final String allUpperCaseUserId = KERMIT_USER.toUpperCase();
    final String actualUserId = KERMIT_USER;
    final ClientAndServer engineMockServer = useAndGetEngineMockServer();

    final List<HttpRequest> mockedRequests = CaseInsensitiveAuthenticationMockUtil.setupCaseInsensitiveAuthentication(
      embeddedOptimizeExtension, engineIntegrationExtension, engineMockServer,
      allUpperCaseUserId, actualUserId
    );

    collectionClient.createNewCollection(actualUserId, actualUserId);
    reportClient.createSingleProcessReportAsUser(null, null, actualUserId, actualUserId);
    reportClient.createSingleDecisionReportAsUser(null, null, actualUserId, actualUserId);
    reportClient.createNewCombinedReportAsUserRawResponse(null, Collections.emptyList(), actualUserId, actualUserId);
    dashboardClient.createDashboardAsUser(null, actualUserId, actualUserId);

    // when
    final List<EntityDto> authorizedEntities = entitiesClient.getAllEntitiesAsUser(allUpperCaseUserId, actualUserId);

    // then
    assertThat(authorizedEntities.size(), is(5));
    assertThat(
      authorizedEntities.stream().map(EntityDto::getCurrentUserRole).collect(Collectors.toList()),
      everyItem(greaterThanOrEqualTo(RoleType.EDITOR))
    );

    mockedRequests.forEach(engineMockServer::verify);
  }

  private ProcessDefinitionEngineDto deploySimpleServiceTaskProcess(final String definitionKey) {
    BpmnModelInstance processModel = Bpmn.createExecutableProcess(definitionKey)
      .name("aProcessName")
      .startEvent()
      .serviceTask()
      .camundaExpression("${true}")
      .endEvent()
      .done();
    return engineIntegrationExtension.deployProcessAndGetProcessDefinition(processModel);
  }

  protected DecisionDefinitionEngineDto deploySimpleDecisionDefinition(final String definitionKey) {
    final DmnModelGenerator dmnModelGenerator = DmnModelGenerator.create()
      .decision()
      .decisionDefinitionKey(definitionKey)
      .addInput("input", "input", "input", DecisionTypeRef.STRING)
      .addOutput("output", DecisionTypeRef.STRING)
      .buildDecision();
    return engineIntegrationExtension.deployDecisionDefinition(dmnModelGenerator.build());
  }

}
