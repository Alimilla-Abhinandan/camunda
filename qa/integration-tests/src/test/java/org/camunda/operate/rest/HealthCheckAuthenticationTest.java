/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.operate.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;

import org.camunda.operate.es.RetryElasticsearchClient;
import org.camunda.operate.property.OperateProperties;
import org.camunda.operate.rest.HealthCheckTest.AddManagementPropertiesInitializer;
import org.camunda.operate.schema.indices.OperateWebSessionIndex;
import org.camunda.operate.util.apps.nobeans.TestApplicationWithNoBeans;
import org.camunda.operate.management.ElsIndicesHealthIndicator;
import org.camunda.operate.webapp.security.ElasticsearchSessionRepository;
import org.camunda.operate.webapp.security.OperateURIs;
import org.camunda.operate.webapp.security.WebSecurityConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests the health check with enabled authentication.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {OperateProperties.class,TestApplicationWithNoBeans.class, ElsIndicesHealthIndicator.class, WebSecurityConfig.class,
      ElasticsearchSessionRepository.class, RetryElasticsearchClient.class, OperateWebSessionIndex.class },
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = AddManagementPropertiesInitializer.class)
@ActiveProfiles(OperateURIs.AUTH_PROFILE)
public class HealthCheckAuthenticationTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @MockBean
  private ElsIndicesHealthIndicator probes;

  @Test
  public void testHealthStateEndpointIsNotSecured() {
    given(probes.getHealth(anyBoolean())).willReturn(Health.up().build());

    final ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
