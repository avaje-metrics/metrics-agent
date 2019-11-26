package org.test.web.api;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MockTimedMetric;
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

    MockTimedMetric metric = MetricManager.testGetTimedMetric("app.IndexResource.get");
    metric.testReset();

    orderService.get();
    assertEquals("app.IndexResource.get", MetricManager.testLastMetricName());
    assertEquals(1, metric.testGetCount());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    orderService.get();
    assertEquals(2, metric.testGetCount());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());
  }

  @Test
  public void testErrorExecution() {

    orderService.testForceError(true);

    MockTimedMetric metric = MetricManager.testGetTimedMetric("app.IndexResource.get");
    metric.testReset();

    try {
      orderService.get();
      fail("Never get here");

    } catch (ResourceException expected) {
      // expecting the error
      assertEquals(1, metric.testGetCount());
      assertTrue(MetricManager.testLastMetricOpcodeError());
    }
  }
}
