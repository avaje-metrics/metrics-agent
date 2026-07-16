package org.test.web.api;

import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSpanControllerTest extends BaseTest {

  @Test
  public void controllerMethodsCanOptIntoTracing() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.tracedEndpoint();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:DefaultSpanController.tracedEndpoint"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("web.api", "label:DefaultSpanController.tracedEndpoint").testIsTraced());
  }

  @Test
  public void controllerMethodsCanOptIntoRootTracing() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.rootEndpoint();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:DefaultSpanController.rootEndpoint"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    assertTrue(Metrics.testGetTimedMetric("web.api", "label:DefaultSpanController.rootEndpoint").testIsTraced());
    assertTrue(Metrics.testGetTimedMetric("web.api", "label:DefaultSpanController.rootEndpoint").testIsRootTraced());
  }

  @Test
  public void controllerMethodsDefaultToPlainTiming() {
    DefaultSpanController controller = new DefaultSpanController();

    Metrics.testReset();
    controller.plainEndpoint();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:DefaultSpanController.plainEndpoint"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasAdd());
    assertFalse(Metrics.testGetTimedMetric("web.api", "label:DefaultSpanController.plainEndpoint").testIsTraced());
  }
}
