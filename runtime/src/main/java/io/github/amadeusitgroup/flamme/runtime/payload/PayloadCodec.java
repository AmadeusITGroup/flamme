package io.github.amadeusitgroup.flamme.runtime.payload;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.Envelope;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;
import java.util.Optional;

public interface PayloadCodec {
  byte[] encodePayload(Any payload);

  Any decodePayload(byte[] data) throws FlammeImplRuntimeError;

  Optional<Envelope.Error> decodeError(byte[] data) throws FlammeImplRuntimeError;
}
