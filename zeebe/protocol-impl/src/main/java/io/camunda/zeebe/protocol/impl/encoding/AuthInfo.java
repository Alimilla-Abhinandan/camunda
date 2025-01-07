/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.protocol.impl.encoding;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.zeebe.msgpack.UnpackedObject;
import io.camunda.zeebe.msgpack.property.DocumentProperty;
import io.camunda.zeebe.msgpack.property.EnumProperty;
import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.util.buffer.BufferUtil;
import java.util.Map;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/** */
public class AuthInfo extends UnpackedObject {

  private final EnumProperty<AuthDataFormat> formatProp =
      new EnumProperty<>("format", AuthDataFormat.class, AuthDataFormat.UNKNOWN);

  private final StringProperty authDataProp = new StringProperty("authData", "");
  private final DocumentProperty claimsProp = new DocumentProperty("claims");

  public AuthInfo() {
    super(3);
    declareProperty(formatProp).declareProperty(authDataProp).declareProperty(claimsProp);
  }

  public AuthDataFormat getFormat() {
    return formatProp.getValue();
  }

  public AuthInfo setFormatProp(final AuthDataFormat format) {
    formatProp.setValue(format);
    return this;
  }

  public DirectBuffer getAuthDataBuffer() {
    return authDataProp.getValue();
  }

  public String getAuthData() {
    return BufferUtil.bufferAsString(authDataProp.getValue());
  }

  public AuthInfo setAuthData(final String authData) {
    authDataProp.setValue(authData);
    return this;
  }

  public Map<String, Object> getClaims() {
    return MsgPackConverter.convertToMap(claimsProp.getValue());
  }

  public AuthInfo setClaims(final DirectBuffer authInfo) {
    claimsProp.setValue(authInfo);
    return this;
  }

  public AuthInfo setClaims(final Map<String, Object> authInfo) {
    claimsProp.setValue(new UnsafeBuffer(MsgPackConverter.convertToMsgPack(authInfo)));
    return this;
  }

  @JsonIgnore
  public DirectBuffer getClaimsBuffer() {
    return claimsProp.getValue();
  }

  public void wrap(final AuthInfo authInfo) {
    formatProp.setValue(authInfo.getFormat());
    authDataProp.setValue(authInfo.getAuthData());
    claimsProp.setValue(authInfo.getClaimsBuffer());
  }

  @Override
  public void reset() {
    formatProp.setValue(AuthDataFormat.UNKNOWN);
    authDataProp.setValue("");
    claimsProp.reset();
  }

  public DirectBuffer toDirectBuffer() {
    final var bytes = new byte[getLength()];
    final var buffer = new UnsafeBuffer(bytes);
    write(buffer, 0);

    return buffer;
  }

  @Override
  public String toString() {
    return "AuthInfo{" + "authData=" + BufferUtil.bufferAsString(claimsProp.getValue()) + '}';
  }

  public enum AuthDataFormat {
    UNKNOWN((short) 0),
    JWT((short) 1);

    public final short id;

    AuthDataFormat(final short id) {
      this.id = id;
    }
  }
}
