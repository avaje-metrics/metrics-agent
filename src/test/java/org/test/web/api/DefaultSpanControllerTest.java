package org.test.web.api;

import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSpanControllerTest extends BaseTest {

  @Test
  public void controllerMethodsCanOptIntoTracing() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.tracedEndpoint();
    assertEquals("web.api.DefaultSpanController.tracedEndpoint", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("web.api.DefaultSpanController.tracedEndpoint").testIsTraced());
  }

  @Test
  public void controllerMethodsCanOptIntoRootTracing() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.rootEndpoint();
    assertEquals("web.api.DefaultSpanController.rootEndpoint", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("web.api.DefaultSpanController.rootEndpoint").testIsTraced());
    assertTrue(Metrics.testGetTimedMetric("web.api.DefaultSpanController.rootEndpoint").testIsRootTraced());
  }

  @Test
  public void controllerMethodsDefaultToPlainTiming() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.plainEndpoint();
    assertEquals("web.api.DefaultSpanController.plainEndpoint", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastOperationWasAdd());
    assertFalse(Metrics.testGetTimedMetric("web.api.DefaultSpanController.plainEndpoint").testIsTraced());
  }
}
