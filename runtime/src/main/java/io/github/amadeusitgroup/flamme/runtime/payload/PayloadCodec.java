package io.github.amadeusitgroup.flamme.runtime.payload;

import io.github.amadeusitgroup.flamme.runtime.FlammeEnvelope;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;

public interface PayloadCodec {
  byte[] encodePayload(FlammeEnvelope envelope);

  FlammeEnvelope decodePayload(byte[] data) throws FlammeImplRuntimeError;
}
