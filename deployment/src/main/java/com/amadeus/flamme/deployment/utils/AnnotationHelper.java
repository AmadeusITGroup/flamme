package com.amadeus.flamme.deployment.utils;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

public class AnnotationHelper {
  public static String[] annotationAsStringArray(AnnotationValue annotationValue) {
    if (annotationValue != null) {
      return annotationValue.asStringArray();
    }
    return new String[0];
  }

  public static AnnotationInstance[] annotationAsNestedArray(AnnotationValue annotationValue) {
    if (annotationValue == null || annotationValue.asNestedArray() == null) {
      return new AnnotationInstance[0];
    }
    return annotationValue.asNestedArray();
  }
}
