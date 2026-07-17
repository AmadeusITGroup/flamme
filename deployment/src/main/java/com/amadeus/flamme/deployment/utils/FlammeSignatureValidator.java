package com.amadeus.flamme.deployment.utils;

import com.amadeus.flamme.deployment.errors.InvalidFlammeComponent;
import com.amadeus.flamme.runtime.annotations.AnnotationData;
import com.amadeus.flamme.runtime.utils.Strings;
import com.google.protobuf.Message;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

public final class FlammeSignatureValidator {
  private static final DotName MAP = DotName.createSimple(Map.class.getName());
  private static final DotName STRING = DotName.createSimple(String.class.getName());
  private static final DotName MESSAGE = DotName.createSimple(Message.class.getName());
  private static final DotName COMPLETABLE_FUTURE =
      DotName.createSimple(CompletableFuture.class.getName());

  public static void validateComponent(
      String interfaceClassName,
      AnnotationData annotationData,
      CombinedIndexBuildItem combinedIndex)
      throws InvalidFlammeComponent {
    ClassInfo classInfo = getClassInfo(interfaceClassName, combinedIndex);
    MethodInfo method = getSingleMethod(classInfo, interfaceClassName);
    validateParameterCount(method, interfaceClassName);

    boolean isProxy = annotationData.consumers().length == 0;
    if (isProxy) {
      validateProxyReturnType(method.returnType(), interfaceClassName);
    } else {
      validateDirectReturnType(method.returnType(), interfaceClassName);
    }

    Type parameterType = method.parameters().get(0).type();
    validateMultipayloadType(parameterType, interfaceClassName);

    if (method.parameters().size() == 2) {
      Type headersType = method.parameters().get(1).type();
      validateHeadersType(headersType, interfaceClassName);
    }
  }

  private static ClassInfo getClassInfo(
      String interfaceClassName, CombinedIndexBuildItem combinedIndex)
      throws InvalidFlammeComponent {

    ClassInfo classInfo =
        combinedIndex.getIndex().getClassByName(DotName.createSimple(interfaceClassName));

    if (classInfo == null) {
      throw new InvalidFlammeComponent(Strings.classNotFoundIndex(interfaceClassName));
    }
    return classInfo;
  }

  private static MethodInfo getSingleMethod(ClassInfo classInfo, String interfaceClassName)
      throws InvalidFlammeComponent {
    Collection<MethodInfo> methods = classInfo.methods();
    if (methods.size() != 1) {
      throw new InvalidFlammeComponent(
          Strings.wrongMethodNumber(methods.size(), interfaceClassName));
    }
    return methods.iterator().next();
  }

  private static void validateParameterCount(MethodInfo method, String interfaceClassName)
      throws InvalidFlammeComponent {
    int count = method.parameters().size();
    if (count != 1 && count != 2) {
      throw new InvalidFlammeComponent(Strings.wrongParameterNumber(count, interfaceClassName));
    }
  }

  private static void validateHeadersType(Type type, String interfaceClassName)
      throws InvalidFlammeComponent {
    ParameterizedType mapType = validateAndGetParameterizedType(type, "headers map");
    if (!mapType.name().equals(MAP)) {
      throw new InvalidFlammeComponent(
          Strings.expected_map(interfaceClassName, mapType.name().toString()));
    }
    List<Type> args = mapType.arguments();
    if (args.size() != 2 || !isOfTypeString(args.get(0)) || !isOfTypeString(args.get(1))) {
      throw new InvalidFlammeComponent(Strings.wrongParameterTypeForHeadersMap(interfaceClassName));
    }
  }

  private static void validateMultipayloadType(Type type, String interfaceClassName)
      throws InvalidFlammeComponent {
    ParameterizedType mapType = validateAndGetParameterizedType(type, "multipayload map");
    if (!mapType.name().equals(MAP)) {
      throw new InvalidFlammeComponent(
          Strings.expected_map(interfaceClassName, mapType.name().toString()));
    }
    List<Type> args = mapType.arguments();
    if (args.size() != 2 || !isOfTypeString(args.get(0)) || !isOfTypeMessage(args.get(1))) {
      throw new InvalidFlammeComponent(
          Strings.wrongParameterTypeForMultipayloadMap(interfaceClassName));
    }
  }

  private static void validateDirectReturnType(Type returnType, String interfaceClassName)
      throws InvalidFlammeComponent {
    if (returnType.kind() == Kind.VOID) {
      return;
    }
    validateMultipayloadType(returnType, interfaceClassName);
  }

  private static ParameterizedType validateAndGetParameterizedType(Type type, String context)
      throws InvalidFlammeComponent {
    if (type.kind() != Kind.PARAMETERIZED_TYPE) {
      throw new InvalidFlammeComponent(Strings.not_parameterized(context, type.kind().toString()));
    }
    return type.asParameterizedType();
  }

  private static void validateProxyReturnType(Type returnType, String interfaceClassName)
      throws InvalidFlammeComponent {

    if (returnType.kind() == Kind.VOID) {
      throw new InvalidFlammeComponent(Strings.GATEWAY_RETURNS_VOID);
    }

    ParameterizedType futureType = validateAndGetParameterizedType(returnType, "return type");

    if (!futureType.name().equals(COMPLETABLE_FUTURE)) {
      throw new InvalidFlammeComponent(Strings.GATEWAY_SHOULD_RETURN_FUTURE);
    }

    Type inner = futureType.arguments().get(0);
    validateMultipayloadType(inner, interfaceClassName);
  }

  private static boolean isOfTypeString(Type type) {
    return type.name() != null && type.name().equals(STRING);
  }

  private static boolean isOfTypeMessage(Type type) {
    return type.name() != null && type.name().equals(MESSAGE);
  }
}
