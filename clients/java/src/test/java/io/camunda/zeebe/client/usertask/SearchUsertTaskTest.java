/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.client.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.protocol.rest.DateFilter;
import io.camunda.zeebe.client.protocol.rest.UserTaskSearchQueryRequest;
import io.camunda.zeebe.client.util.ClientRestTest;
import org.junit.jupiter.api.Test;

public final class SearchUsertTaskTest extends ClientRestTest {

  @Test
  void shouldSearchUserTask() {
    // when
    client.newUserTaskQuery().send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter()).isNull();
  }

  @Test
  void shouldSearchUserTaskByAssignee() {
    // when
    client.newUserTaskQuery().filter(f -> f.assignee("demo")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getAssignee()).isEqualTo("demo");
  }

  @Test
  void shouldSearchUserTaskByState() {
    // when
    client.newUserTaskQuery().filter(f -> f.state("completed")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getTaskState()).isEqualTo("completed");
  }

  @Test
  void shouldSearchUserTaskByKey() {
    // when
    client.newUserTaskQuery().filter(f -> f.key(12345L)).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getUserTaskKey()).isEqualTo(12345L);
  }

  @Test
  void shouldSearchUserTaskByTaskDefinitionId() {
    // when
    client.newUserTaskQuery().filter(f -> f.elementId("task-def-id")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getElementId()).isEqualTo("task-def-id");
  }

  @Test
  void shouldSearchUserTaskByCandidateGroup() {
    // when
    client.newUserTaskQuery().filter(f -> f.candidateGroup("group1")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getCandidateGroup()).isEqualTo("group1");
  }

  @Test
  void shouldSearchUserTaskByCandidateUser() {
    // when
    client.newUserTaskQuery().filter(f -> f.candidateUser("user1")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getCandidateUser()).isEqualTo("user1");
  }

  @Test
  void shouldSearchUserTaskByProcessDefinitionKey() {
    // when
    client.newUserTaskQuery().filter(f -> f.processDefinitionKey(123L)).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getProcessDefinitionKey()).isEqualTo(123L);
  }

  @Test
  void shouldSearchUserTaskByProcessInstanceKey() {
    // when
    client.newUserTaskQuery().filter(f -> f.processInstanceKey(456L)).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getProcessInstanceKey()).isEqualTo(456L);
  }

  @Test
  void shouldSearchUserTaskByFollowUpDate() {
    // when
    client
        .newUserTaskQuery()
        .filter(
            f ->
                f.followUpDate(
                    new DateFilter().from("2023-10-01T00:00:00Z").to("2023-10-01T00:00:00Z")))
        .send()
        .join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getFollowUpDate().getFrom()).isEqualTo("2023-10-01T00:00:00Z");
    assertThat(request.getFilter().getFollowUpDate().getTo()).isEqualTo("2023-10-01T00:00:00Z");
  }

  @Test
  void shouldSearchUserTaskByDueDate() {
    // when
    client
        .newUserTaskQuery()
        .filter(
            f ->
                f.dueDate(new DateFilter().from("2023-10-01T00:00:00Z").to("2023-10-01T00:00:00Z")))
        .send()
        .join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getDueDate().getFrom()).isEqualTo("2023-10-01T00:00:00Z");
    assertThat(request.getFilter().getDueDate().getTo()).isEqualTo("2023-10-01T00:00:00Z");
  }

  @Test
  void shouldSearchUserTaskByCreationDate() {
    // when
    client
        .newUserTaskQuery()
        .filter(
            f ->
                f.creationDate(
                    new DateFilter().from("2023-10-01T00:00:00Z").to("2023-10-01T00:00:00Z")))
        .send()
        .join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getCreationDate().getFrom()).isEqualTo("2023-10-01T00:00:00Z");
    assertThat(request.getFilter().getCreationDate().getTo()).isEqualTo("2023-10-01T00:00:00Z");
  }

  @Test
  void shouldSearchUserTaskByCompletionDate() {
    // when
    client
        .newUserTaskQuery()
        .filter(
            f ->
                f.completionDate(
                    new DateFilter().from("2023-10-01T00:00:00Z").to("2023-10-01T00:00:00Z")))
        .send()
        .join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getCompletionDate().getFrom()).isEqualTo("2023-10-01T00:00:00Z");
    assertThat(request.getFilter().getCompletionDate().getTo()).isEqualTo("2023-10-01T00:00:00Z");
  }

  @Test
  void shouldSearchUserTaskByTenantId() {
    // when
    client.newUserTaskQuery().filter(f -> f.tentantId("tenant1")).send().join();

    // then
    final UserTaskSearchQueryRequest request =
        gatewayService.getLastRequest(UserTaskSearchQueryRequest.class);
    assertThat(request.getFilter().getTenantIds()).isEqualTo("tenant1");
  }
}
