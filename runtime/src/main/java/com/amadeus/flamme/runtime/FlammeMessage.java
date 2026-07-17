package com.amadeus.flamme.runtime;

import com.google.protobuf.Message;
import java.util.Map;

public record FlammeMessage(
    Map<String, Message> payload, Map<String, String> headers, String replyTo) {
  FlammeMessage wrap(Map<String, Message> payload) {
    return new FlammeMessage(payload, this.headers(), this.replyTo());
  }
}
