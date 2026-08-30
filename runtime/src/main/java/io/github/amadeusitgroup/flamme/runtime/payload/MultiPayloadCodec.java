package io.github.amadeusitgroup.flamme.runtime.payload;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.amadeusitgroup.flamme.runtime.FlammeEnvelope;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;
import io.github.amadeusitgroup.flamme.runtime.utils.Strings;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MultiPayloadCodec implements PayloadCodec {

  @Override
  public byte[] encodePayload(FlammeEnvelope envelope) {
    return envelope.toByteArray();
  }

  @Override
  public FlammeEnvelope decodePayload(byte[] data) throws FlammeImplRuntimeError {
    try {
      return FlammeEnvelope.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      throw new FlammeImplRuntimeError(Strings.ERROR_DECODING_REMOTE_PAYLOAD, e);
    }
  }
}
