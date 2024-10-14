/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.authorization;

import static io.camunda.zeebe.protocol.record.Assertions.assertThat;

import io.camunda.zeebe.engine.util.EngineRule;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.test.util.record.RecordingExporterTestWatcher;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class RoleTest {

  @ClassRule public static final EngineRule ENGINE = EngineRule.singlePartition();

  @Rule public final TestWatcher recordingExporterTestWatcher = new RecordingExporterTestWatcher();

  @Test
  public void shouldCreateRole() {
    final var roleRecord = ENGINE.role().newRole("Role").create();

    final var createdRole = roleRecord.getValue();
    Assertions.assertThat(createdRole).isNotNull().hasFieldOrPropertyWithValue("name", "Role");
  }

  @Test
  public void shouldNotDuplicate() {
    // given
    final var name = UUID.randomUUID().toString();
    final var roleRecord = ENGINE.role().newRole(name).create();

    // when
    final var duplicatedRoleRecord = ENGINE.role().newRole(name).expectRejection().create();

    final var createdRole = roleRecord.getValue();
    Assertions.assertThat(createdRole).isNotNull().hasFieldOrPropertyWithValue("name", name);

    assertThat(duplicatedRoleRecord)
        .hasRejectionType(RejectionType.ALREADY_EXISTS)
        .hasRejectionReason(
            "Expected to create role with name '"
                + name
                + "', but a role with this name already exists");
  }
}
