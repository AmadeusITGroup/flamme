package io.github.amadeusitgroup.flamme.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.github.amadeusitgroup.flamme.test.fixtures.Components;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ComponentFailureTest {

  @Inject Components.Gateway gateway;

  @RegisterExtension
  static final QuarkusUnitTest unitTest =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClass(Components.Gateway.class)
                      .addClass(Components.FailingComponent.class)
                      .addClass(Components.FailingComponentImpl.class));

  @Test
  void futureShouldCompleteExceptionallyWhenComponentThrows() {
    Any payload = Any.pack(StringValue.of("Hello World"));
    CompletableFuture<Any> resultFuture = gateway.call(payload);
    CompletionException ex = assertThrows(CompletionException.class, resultFuture::join);
    assertTrue(ex.getMessage().contains("failing component failed"));
  }
}
