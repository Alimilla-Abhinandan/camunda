/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.rest;

import io.camunda.optimize.dto.optimize.query.ui_configuration.UIConfigurationResponseDto;
import io.camunda.optimize.service.UIConfigurationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api" + UIConfigurationRestService.UI_CONFIGURATION_PATH)
public class UIConfigurationRestService {

  public static final String UI_CONFIGURATION_PATH = "/ui-configuration";

  private final UIConfigurationService uiConfigurationService;

  public UIConfigurationRestService(final UIConfigurationService uiConfigurationService) {
    this.uiConfigurationService = uiConfigurationService;
  }

  @GetMapping()
  public UIConfigurationResponseDto getUIConfiguration() {
    return uiConfigurationService.getUIConfiguration();
  }
}
