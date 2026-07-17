package com.amadeus.flamme.runtime.transport;

import java.util.Map;

public record TransportMessage(
    String subject, Map<String, String> headers, byte[] data, String replyTo) {}
