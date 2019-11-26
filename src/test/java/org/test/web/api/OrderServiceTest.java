package org.test.web.api;

import io.avaje.metrics.MockTimedMetric;
import io.avaje.metrics.MetricManager;
import org.junit.Assert;
import org.junit.Test;
import org.test.app.service.OrderService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderServiceTest extends BaseTest {

  @Test
  public void testSuccessExecution() throws InterruptedException {

    OrderService orderService = new OrderService();

    MockTimedMetric metric = MetricManager.testGetTimedMetric("service.OrderService.processOrders");
    metric.testReset();
    MetricManager.testReset();

    assertEquals(0, metric.testGetCount());

    orderService.processOrders();
    assertEquals(1, metric.testGetCount());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    assertEquals(2, metric.testGetCount());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    assertEquals(3, metric.testGetCount());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

  }
}
