/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.schema.index;

import org.camunda.optimize.upgrade.es.ElasticsearchConstants;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcessDefinitionIndex extends AbstractDefinitionIndex {

  public static final int VERSION = 2;

  public static final String PROCESS_DEFINITION_ID = DEFINITION_ID;
  public static final String PROCESS_DEFINITION_KEY = DEFINITION_KEY;
  public static final String PROCESS_DEFINITION_VERSION = DEFINITION_VERSION;
  public static final String PROCESS_DEFINITION_VERSION_TAG = DEFINITION_VERSION_TAG;
  public static final String PROCESS_DEFINITION_NAME = DEFINITION_NAME;
  public static final String PROCESS_DEFINITION_XML = "bpmn20Xml";
  public static final String FLOW_NODE_NAMES = "flowNodeNames";
  public static final String USER_TASK_NAMES = "userTaskNames";
  public static final String ENGINE = DEFINITION_ENGINE;
  public static final String TENANT_ID = DEFINITION_TENANT_ID;

  @Override
  public String getIndexName() {
    return ElasticsearchConstants.PROCESS_DEFINITION_INDEX_NAME;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public XContentBuilder addProperties(XContentBuilder xContentBuilder) throws IOException {
    // @formatter:off
    return super.addProperties(xContentBuilder)
      .startObject(FLOW_NODE_NAMES)
        .field("type", "object")
        .field("enabled", "false")
      .endObject()
      .startObject(USER_TASK_NAMES)
        .field("type", "object")
        .field("enabled", "false")
      .endObject()
      .startObject(PROCESS_DEFINITION_XML)
        .field("type", "text")
        .field("index", true)
        .field("analyzer", "is_present_analyzer")
      .endObject();
    // @formatter:on
  }

}
