package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockTimer;
import org.junit.Test;
import org.test.app.SimpleService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IndexResourceTest extends BaseTest {

  private final IndexResource orderService = new IndexResource(new SimpleService());

  @Test
  public void testSuccessExecution() {

    orderService.testForceError(false);

    MockTimer metric = Metrics.testGetTimedMetric("app.IndexResource.get");
    metric.testReset();

    orderService.get();
    assertEquals("app.IndexResource.get", Metrics.testLastMetricName());
    assertEquals(1, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    orderService.get();
    assertEquals(2, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());
  }

  @Test
  public void testErrorExecution() {

    orderService.testForceError(true);

    MockTimer metric = Metrics.testGetTimedMetric("app.IndexResource.get");
    metric.testReset();

    try {
      orderService.get();
      fail("Never get here");

    } catch (ResourceException expected) {
      // expecting the error
      assertEquals(1, metric.testGetCount());
      assertTrue(Metrics.testLastMetricOpcodeError());
    }
  }
}
