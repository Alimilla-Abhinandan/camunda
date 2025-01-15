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
package io.camunda.zeebe.client.api.command;

public class ClientHttpException extends ClientException {
  private final int code;
  private final String reason;

  public ClientHttpException(
      final String message, final int code, final String reason, final Throwable cause) {
    super(message, cause);
    this.code = code;
    this.reason = reason;
  }

  public ClientHttpException(final String message, final int code, final String reason) {
    super(message);
    this.code = code;
    this.reason = reason;
  }

  public ClientHttpException(final int code, final String reason) {
    this(String.format("Failed with code %d: '%s'", code, reason), code, reason);
  }

  public int code() {
    return code;
  }

  public String reason() {
    return reason;
  }
}
