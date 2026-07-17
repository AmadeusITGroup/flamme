package com.amadeus.flamme.runtime.payload;

import com.amadeus.flamme.runtime.errors.FlammeImplRuntimeError;
import com.amadeus.flamme.runtime.utils.Strings;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class MultiPayloadCodec implements PayloadCodec {

  private record WireEntry(String className, byte[] bytes) {}

  @Override
  public byte[] encodePayload(Map<String, Message> payload) throws FlammeImplRuntimeError {
    try {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(byteStream);
      out.writeInt(payload.size());
      for (Map.Entry<String, Message> entry : payload.entrySet()) {
        out.writeUTF(entry.getKey());
        out.writeUTF(entry.getValue().getClass().getName());
        byte[] bytes = entry.getValue().toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
      }
      out.flush();
      return byteStream.toByteArray();
    } catch (IOException e) {
      throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Message> decodePayload(byte[] data, Map<String, String> multipayloadKeys)
      throws FlammeImplRuntimeError {
    Map<String, WireEntry> entries = decodeEntries(data);
    Map<String, Message> decodedPayload = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : multipayloadKeys.entrySet()) {
      WireEntry wireEntry = entries.get(entry.getKey());
      if (wireEntry == null) {
        throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
      }
      try {
        Class<?> loadedClass =
            Thread.currentThread().getContextClassLoader().loadClass(entry.getValue());
        if (!Message.class.isAssignableFrom(loadedClass)) {
          throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
        }
        if (!entry.getValue().equals(wireEntry.className())) {
          throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
        }
        Class<? extends Message> protobufClass = (Class<? extends Message>) loadedClass;
        Message decoded = parseMessage(protobufClass, wireEntry.bytes());
        decodedPayload.put(entry.getKey(), decoded);
      } catch (ClassNotFoundException exception) {
        throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD, exception);
      }
    }
    return decodedPayload;
  }

  @Override
  public Optional<String> decodeError(byte[] data) throws FlammeImplRuntimeError {
    Map<String, WireEntry> entries = decodeEntries(data);
    WireEntry wireEntry = entries.get(Strings.ERROR_KEY);
    if (wireEntry == null) {
      return Optional.empty();
    }
    if (!StringValue.class.getName().equals(wireEntry.className())) {
      throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
    }
    StringValue value = parseMessage(StringValue.class, wireEntry.bytes());
    return Optional.of(value.getValue());
  }

  private Map<String, WireEntry> decodeEntries(byte[] data) throws FlammeImplRuntimeError {
    try {
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
      int size = in.readInt();
      if (size < 0) {
        throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
      }
      Map<String, WireEntry> entries = new LinkedHashMap<>();
      for (int i = 0; i < size; i++) {
        String key = in.readUTF();
        String className = in.readUTF();
        int length = in.readInt();
        if (length < 0) {
          throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
        }
        byte[] bytes = in.readNBytes(length);
        if (bytes.length != length) {
          throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
        }
        entries.put(key, new WireEntry(className, bytes));
      }
      if (in.available() != 0) {
        throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD);
      }
      return entries;
    } catch (IOException e) {
      throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Message> T parseMessage(Class<T> protobufClass, byte[] bytes)
      throws FlammeImplRuntimeError {
    try {
      Method parserMethod = protobufClass.getMethod("parser");
      com.google.protobuf.Parser<T> parser =
          (com.google.protobuf.Parser<T>) parserMethod.invoke(null);
      return parser.parseFrom(bytes);
    } catch (ReflectiveOperationException | com.google.protobuf.InvalidProtocolBufferException e) {
      throw new FlammeImplRuntimeError(Strings.FAILED_TO_READ_PAYLOAD, e);
    }
  }
}
