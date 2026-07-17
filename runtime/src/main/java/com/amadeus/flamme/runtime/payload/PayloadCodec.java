package com.amadeus.flamme.runtime.payload;

import com.amadeus.flamme.runtime.errors.FlammeImplRuntimeError;
import com.google.protobuf.Message;
import java.util.Map;
import java.util.Optional;

public interface PayloadCodec {
  byte[] encodePayload(Map<String, Message> payload) throws FlammeImplRuntimeError;

  Map<String, Message> decodePayload(byte[] data, Map<String, String> multipayloadKeys)
      throws FlammeImplRuntimeError;

  Optional<String> decodeError(byte[] data) throws FlammeImplRuntimeError;
}
