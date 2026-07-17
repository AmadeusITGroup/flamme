package com.amadeus.flamme.runtime.annotations;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Qualifier
public @interface FlammeImpl {
  class Literal extends AnnotationLiteral<FlammeImpl> implements FlammeImpl {
    public static final Literal INSTANCE = new Literal();
  }
}
