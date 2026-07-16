package org.test.web.api;

import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
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
    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:SpanTimedResource.tracedMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("spanapi", "label:SpanTimedResource.tracedMethod").testIsTraced());

    Metrics.testReset();
    resource.plainMethod();
    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:SpanTimedResource.plainMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasAdd());
    assertFalse(Metrics.testGetTimedMetric("spanapi", "label:SpanTimedResource.plainMethod").testIsTraced());
  }

  @Test
  public void rootMethodUsesRootTracedTimer() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    resource.rootMethod();
    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:SpanTimedResource.rootMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("spanapi", "label:SpanTimedResource.rootMethod").testIsTraced());
    assertTrue(Metrics.testGetTimedMetric("spanapi", "label:SpanTimedResource.rootMethod").testIsRootTraced());
  }

  @Test
  public void tracedBucketMethodUsesTracedTimer() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    resource.tracedBucketMethod();
    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:SpanTimedResource.tracedBucketMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetBucketTimedMetric("spanapi", "label:SpanTimedResource.tracedBucketMethod").testIsTraced());
  }

  @Test
  public void tracedErrorUsesEventErrorPath() {
    SpanTimedResource resource = new SpanTimedResource();

    Metrics.testReset();
    try {
      resource.tracedError();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("spanapi", Metrics.testLastMetricName());
      assertArrayEquals(new String[]{"label:SpanTimedResource.tracedError"}, Metrics.testLastMetricTags());
      assertTrue(Metrics.testLastMetricOpcodeError());
      assertEquals("event.endWithError", Metrics.testLastOperationKind());
      assertSame(expected, Metrics.testLastThrowable());
    }
  }
}
