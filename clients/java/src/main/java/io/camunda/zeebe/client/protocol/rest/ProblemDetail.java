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
package io.camunda.zeebe.client.protocol.rest;

/**
 * Added to keep compatibility with the previous version of the client. Used in {@link
 * io.camunda.zeebe.client.api.command.ProblemException#details()}.
 */
public class ProblemDetail extends io.camunda.client.protocol.rest.ProblemDetail {
  public static ProblemDetail of(
      final io.camunda.client.protocol.rest.ProblemDetail problemDetail) {
    if (problemDetail == null) {
      return null;
    }
    final ProblemDetail copy = new ProblemDetail();
    copy.type(problemDetail.getType())
        .title(problemDetail.getTitle())
        .detail(problemDetail.getDetail())
        .instance(problemDetail.getInstance())
        .status(problemDetail.getStatus());
    return copy;
  }
}
