package io.github.amadeusitgroup.flamme.runtime;

import com.google.protobuf.Any;
import java.util.Map;

public record FlammeMessage(FlammeEnvelope envelope, Map<String, String> headers, String replyTo) {
  FlammeMessage wrap(Any payload) {
    FlammeEnvelope envelope = FlammeEnvelope.newBuilder().setPayload(payload).build();
    return new FlammeMessage(envelope, this.headers(), this.replyTo());
  }

  FlammeMessage error(FlammeError error) {
    FlammeEnvelope envelope = FlammeEnvelope.newBuilder().setError(error).build();
    return new FlammeMessage(envelope, headers, replyTo);
  }
}
