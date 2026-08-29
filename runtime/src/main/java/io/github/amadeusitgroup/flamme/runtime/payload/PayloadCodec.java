package io.github.amadeusitgroup.flamme.runtime.payload;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.FlammeError;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;
import java.util.Optional;

public interface PayloadCodec {
  byte[] encodePayload(Any payload);

  Any decodePayload(byte[] data) throws FlammeImplRuntimeError;

  Optional<FlammeError> decodeError(byte[] data) throws FlammeImplRuntimeError;
}
