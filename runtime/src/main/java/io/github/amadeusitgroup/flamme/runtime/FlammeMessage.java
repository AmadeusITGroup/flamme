package io.github.amadeusitgroup.flamme.runtime;

import com.google.protobuf.Any;
import java.util.Map;

public record FlammeMessage(Any payload, Map<String, String> headers, String replyTo) {
  FlammeMessage wrap(Any payload) {
    return new FlammeMessage(payload, this.headers(), this.replyTo());
  }
}
