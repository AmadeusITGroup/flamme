package com.amadeus.flamme.runtime.utils;

public final class Strings {

  public static final String CLASS_NOT_FOUND_IN_INDEX = "class not found in index %s";
  public static final String WRONG_METHOD_NUMBER =
      "flamme components should declare exactly one method found %d for %s";
  public static final String WRONG_PARAMETER_NUMBER =
      "flamme methods should accept one or two parameters found %d for %s";
  public static final String NOT_PARAMETERIZED = "%s must be parameterized found %s";
  public static final String EXPECTED_MAP =
      "expected map for headers parameter in interface %s found %s";
  public static final String WRONG_PARAMETER_TYPE_FOR_HEADERS_MAP =
      "map must be parameterized as map <string, string> for interface %s";
  public static final String WRONG_PARAMETER_TYPE_FOR_MULTIPAYLOAD_MAP =
      "map must be parameterized as map <string, message> for interface %s";

  public static final String GATEWAY_RETURNS_VOID = "flamme gateways cannot return void";
  public static final String GATEWAY_SHOULD_RETURN_FUTURE =
      "flamme gateways must return completable future with parameters <string, message>";
  public static final String CLASS_NOT_FOUND_THREAD_CONTEXT =
      "service interface not found in thread context %s";
  public static final String FLAMME_IMPL_RESOLUTION_ERROR =
      "no implementation found for %s qualified with @FlammeImpl";
  public static final String INVOKATION_ERROR = "error invoking %s";
  public static final String NATS_CONNECTION_ERROR = "there was an error connecting to NATS";
  public static final String NATS_NOT_CONFIGURED =
      "nats is not configured, remote services and subscriptions will not be available";
  public static final String FAILED_TO_READ_PAYLOAD =
      "there was an error when reading the protobuf payload from the incoming nats message";
  public static final String FAILED_TO_PROCESS_MESSAGE = "failed to porcess message on subject %s";
  public static final String ERROR_WHEN_RECEIVING_REPLY =
      "there was an unexpected error when receiving the reply message";
  public static final String FUTURE_DID_NOT_COMPLETE = "future did not complete in time";
  public static final String THREAD_WAS_INTERRUPTED = "the main thread was interrupted";

  public static final String classNotFoundIndex(String className) {
    return String.format(CLASS_NOT_FOUND_IN_INDEX, className);
  }

  public static final String ERROR_KEY = "FLAMME_ERROR";

  public static final String REPLY_TO_PREFIX = "INBOX.";
  ;

  public static final String wrongMethodNumber(int methodNumber, String className) {
    return String.format(WRONG_METHOD_NUMBER, methodNumber, className);
  }

  public static final String wrongParameterNumber(int count, String className) {
    return String.format(WRONG_PARAMETER_NUMBER, count, className);
  }

  public static final String not_parameterized(String context, String kind) {
    return String.format(NOT_PARAMETERIZED, context, kind);
  }

  public static final String expected_map(String className, String type) {
    return String.format(EXPECTED_MAP, className, type);
  }

  public static final String wrongParameterTypeForHeadersMap(String className) {
    return String.format(WRONG_PARAMETER_TYPE_FOR_HEADERS_MAP, className);
  }

  public static final String wrongParameterTypeForMultipayloadMap(String className) {
    return String.format(WRONG_PARAMETER_TYPE_FOR_MULTIPAYLOAD_MAP, className);
  }

  public static final String classNotFoundThreadContext(String className) {
    return String.format(CLASS_NOT_FOUND_THREAD_CONTEXT, className);
  }

  public static final String flammeImplResolutionError(String className) {
    return String.format(FLAMME_IMPL_RESOLUTION_ERROR, className);
  }

  public static final String invokationError(String className) {
    return String.format(INVOKATION_ERROR, className);
  }

  public static final String failedToProcessMessage(String subject) {
    return String.format(FAILED_TO_PROCESS_MESSAGE, subject);
  }
}
