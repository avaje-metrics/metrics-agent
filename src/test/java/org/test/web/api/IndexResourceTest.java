package org.test.web.api;

import org.avaje.metric.MockTimedMetric;
import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Test;
import org.test.app.SimpleService;

public class IndexResourceTest extends BaseTest {

  IndexResource orderService = new IndexResource(new SimpleService());
  
  MockTimedMetric metric = MetricManager.testGetTimedMetric("web.api.IndexResource.index");

  @Test
  public void testSuccessExecution() {

    orderService.testForceError(false);
    metric.testReset();
    
    orderService.index();
    Assert.assertEquals("web.api.IndexResource.index", MetricManager.testLastMetricName());
    Assert.assertEquals(1, metric.testGetCount());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());
    
    orderService.index();
    Assert.assertEquals(2, metric.testGetCount());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());
  }
  
  @Test
  public void testErrorExecution() {

    orderService.testForceError(true);
    metric.testReset();
    
    try {
      orderService.index();
      Assert.assertTrue("Never get here", false);
      
    } catch (ResourceException expected) {
      // expecting the error 
      Assert.assertEquals(1, metric.testGetCount());
      Assert.assertTrue(MetricManager.testLastMetricOpcodeError());
    }
  }
}
