package org.test.web.api;

import io.avaje.metrics.MockTimer;
import io.avaje.metrics.Metrics;
import org.junit.Test;
import org.test.app.service.OrderService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderServiceTest extends BaseTest {

  @Test
  public void testSuccessExecution() throws InterruptedException {

    OrderService orderService = new OrderService();

    MockTimer metric = Metrics.testGetTimedMetric("service.OrderService.processOrders");
    metric.testReset();
    Metrics.testReset();

    assertEquals(0, metric.testGetCount());

    orderService.processOrders();
    assertEquals(1, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    assertEquals(2, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    assertEquals(3, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

  }
}
