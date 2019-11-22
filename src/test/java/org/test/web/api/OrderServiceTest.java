package org.test.web.api;

import io.avaje.metrics.MockTimedMetric;
import io.avaje.metrics.MetricManager;
import org.junit.Assert;
import org.junit.Test;
import org.test.app.service.OrderService;

public class OrderServiceTest extends BaseTest {

  @Test
  public void testSuccessExecution() throws InterruptedException {

    OrderService orderService = new OrderService();

    MockTimedMetric metric = MetricManager.testGetTimedMetric("service.OrderService.processOrders");
    metric.testReset();
    MetricManager.testReset();

    Assert.assertEquals(0, metric.testGetCount());

    orderService.processOrders();
    Assert.assertEquals(1, metric.testGetCount());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    Assert.assertEquals(2, metric.testGetCount());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    orderService.processOrders();
    Assert.assertEquals(3, metric.testGetCount());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

  }
}
