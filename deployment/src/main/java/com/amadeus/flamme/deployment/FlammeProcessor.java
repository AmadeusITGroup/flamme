package com.amadeus.flamme.deployment;

import com.amadeus.flamme.deployment.errors.InvalidFlammeComponent;
import com.amadeus.flamme.deployment.utils.AnnotationHelper;
import com.amadeus.flamme.deployment.utils.FlammeSignatureValidator;
import com.amadeus.flamme.runtime.Broker;
import com.amadeus.flamme.runtime.ConnectionInitializer;
import com.amadeus.flamme.runtime.FlammeRecorder;
import com.amadeus.flamme.runtime.annotations.AnnotationData;
import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.amadeus.flamme.runtime.errors.FlammeServiceRegistrationError;
import com.amadeus.flamme.runtime.payload.MultiPayloadCodec;
import com.amadeus.flamme.runtime.transport.NatsClientManager;
import com.amadeus.flamme.runtime.transport.NatsTransportClient;
import com.amadeus.flamme.runtime.utils.Strings;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

class FlammeProcessor {

  private static final String FEATURE = "flamme";
  private static final DotName FLAMME_ANNOTATION = DotName.createSimple(Flamme.class.getName());

  @BuildStep
  AdditionalBeanBuildItem registerQualifier() {
    return new AdditionalBeanBuildItem(FlammeImpl.class);
  }

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  AdditionalBeanBuildItem registerBroker() {
    return AdditionalBeanBuildItem.unremovableOf(Broker.class);
  }

  @BuildStep
  AdditionalBeanBuildItem registerTransportAndCodecBeans() {
    return AdditionalBeanBuildItem.builder()
        .setUnremovable()
        .addBeanClasses(NatsClientManager.class, NatsTransportClient.class, MultiPayloadCodec.class)
        .build();
  }

  @BuildStep
  AdditionalBeanBuildItem registerInitializer() {
    return AdditionalBeanBuildItem.unremovableOf(ConnectionInitializer.class);
  }

  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void registerServicesAndSyntheticBeans(
      FlammeRecorder recorder,
      CombinedIndexBuildItem combinedIndex,
      BuildProducer<SyntheticBeanBuildItem> syntheticBeans)
      throws InvalidFlammeComponent, FlammeServiceRegistrationError {
    Collection<AnnotationInstance> flammeAnnotations =
        combinedIndex.getIndex().getAnnotations(FLAMME_ANNOTATION);
    for (AnnotationInstance annotation : flammeAnnotations) {
      String interfaceClassName = annotation.target().asClass().name().toString();
      AnnotationData annotationData = validateAndGetAnnotationData(annotation, combinedIndex);
      FlammeSignatureValidator.validateComponent(interfaceClassName, annotationData, combinedIndex);
      @SuppressWarnings("unused")
      Class<?> interfaceClass;
      try {
        interfaceClass =
            Thread.currentThread().getContextClassLoader().loadClass(interfaceClassName);
      } catch (ClassNotFoundException e) {
        throw new InvalidFlammeComponent(Strings.classNotFoundThreadContext(interfaceClassName), e);
      }
      recorder.registerService(interfaceClassName, annotationData);
      boolean isProxy = annotationData.consumers().length == 0;
      if (isProxy) {
        syntheticBeans.produce(
            SyntheticBeanBuildItem.configure(interfaceClass)
                .scope(Singleton.class)
                .supplier(recorder.createProxyInstance(interfaceClassName, annotationData))
                .setRuntimeInit()
                .done());
      }
    }
  }

  public AnnotationData validateAndGetAnnotationData(
      AnnotationInstance annotation, CombinedIndexBuildItem combinedIndex)
      throws InvalidFlammeComponent {
    String componentName = annotation.value("serviceName").asString();
    String[] producers = AnnotationHelper.annotationAsStringArray(annotation.value("produces"));
    String[] consumers = AnnotationHelper.annotationAsStringArray(annotation.value("consumes"));
    Map<String, String> multipayloadKeys = new HashMap<>();
    AnnotationInstance[] annotationInstances =
        AnnotationHelper.annotationAsNestedArray(annotation.value("multiPayloadKeys"));
    for (AnnotationInstance instance : annotationInstances) {
      AnnotationValue idValue = instance.value("id");
      AnnotationValue typeValue = instance.value("type");
      if (idValue == null || typeValue == null) continue;
      String id = idValue.asString();
      DotName dotName = typeValue.asClass().name();
      String fqcn = dotName.toString();
      try {
        Thread.currentThread().getContextClassLoader().loadClass(fqcn);
      } catch (ClassNotFoundException e) {
        throw new InvalidFlammeComponent(Strings.classNotFoundIndex(dotName.toString()), e);
      }
      multipayloadKeys.put(id, fqcn);
    }
    ;
    return new AnnotationData(componentName, producers, consumers, multipayloadKeys);
  }
}
