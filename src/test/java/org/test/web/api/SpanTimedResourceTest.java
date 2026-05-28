package org.test.web.api;

import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpanTimedResourceTest extends BaseTest {

  @Test
  public void tracedAndPlainMethodsUseDifferentPaths() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    resource.tracedMethod();
    assertEquals("spanapi.SpanTimedResource.tracedMethod", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("spanapi.SpanTimedResource.tracedMethod").testIsTraced());

    Metrics.testReset();
    resource.plainMethod();
    assertEquals("spanapi.SpanTimedResource.plainMethod", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasAdd());
    assertFalse(Metrics.testGetTimedMetric("spanapi.SpanTimedResource.plainMethod").testIsTraced());
  }

  @Test
  public void rootMethodUsesRootTracedTimer() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    resource.rootMethod();
    assertEquals("spanapi.SpanTimedResource.rootMethod", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("spanapi.SpanTimedResource.rootMethod").testIsTraced());
    assertTrue(Metrics.testGetTimedMetric("spanapi.SpanTimedResource.rootMethod").testIsRootTraced());
  }

  @Test
  public void tracedBucketMethodUsesTracedTimer() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    resource.tracedBucketMethod();
    assertEquals("spanapi.SpanTimedResource.tracedBucketMethod", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetBucketTimedMetric("spanapi.SpanTimedResource.tracedBucketMethod").testIsTraced());
  }

  @Test
  public void tracedErrorUsesEventErrorPath() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    try {
      resource.tracedError();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("spanapi.SpanTimedResource.tracedError", Metrics.testLastMetricName());
      assertTrue(Metrics.testLastMetricOpcodeError());
      assertEquals("event.endWithError", Metrics.testLastOperationKind());
      assertSame(expected, Metrics.testLastThrowable());
    }
  }
}
