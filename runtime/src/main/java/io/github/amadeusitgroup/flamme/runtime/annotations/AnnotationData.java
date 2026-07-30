package io.github.amadeusitgroup.flamme.runtime.annotations;

import java.util.Map;

public record AnnotationData(
    String serviceName,
    String[] producers,
    String[] consumers,
    Map<String, String> multipayloadKeys) {}
