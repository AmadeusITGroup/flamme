package io.github.amadeusitgroup.flamme.runtime.payload;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.amadeusitgroup.flamme.runtime.FlammeEnvelope;
import io.github.amadeusitgroup.flamme.runtime.FlammeError;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;
import io.github.amadeusitgroup.flamme.runtime.utils.Strings;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class MultiPayloadCodec implements PayloadCodec {

  @Override
  public byte[] encodePayload(Any payload) {
    return FlammeEnvelope.newBuilder().setPayload(payload).build().toByteArray();
  }

  @Override
  public Any decodePayload(byte[] data) throws FlammeImplRuntimeError {
    try {
      return FlammeEnvelope.parseFrom(data).getPayload();
    } catch (InvalidProtocolBufferException e) {
      throw new FlammeImplRuntimeError(Strings.ERROR_DECODING_REMOTE_PAYLOAD, e);
    }
  }

  @Override
  public Optional<FlammeError> decodeError(byte[] data) throws FlammeImplRuntimeError {
    try {
      var envelope = FlammeEnvelope.parseFrom(data);
      return envelope.hasError() ? Optional.of(envelope.getError()) : Optional.empty();
    } catch (InvalidProtocolBufferException e) {
      throw new FlammeImplRuntimeError(Strings.ERROR_DECODING_REMOTE_PAYLOAD, e);
    }
  }
}
