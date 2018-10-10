package org.test.web.api;

import org.avaje.metric.MetricManager;
import org.avaje.metric.MockBucketTimedMetric;
import org.junit.Assert;
import org.junit.Test;
import org.test.app.OtherSimpleService;

public class BucketTimedMetricTest extends BaseTest {

  @Test
  public void testExecutionCount() throws InterruptedException {

    OtherSimpleService service = new OtherSimpleService();

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());

    service.sayHi();
    Assert.assertEquals("OtherSimpleService.sayHi", MetricManager.testLastMetricName());

    MockBucketTimedMetric metric = MetricManager.testGetBucketTimedMetric("OtherSimpleService.saySomethingElse");
    metric.testReset();
    Assert.assertEquals(0, metric.testGetCount());

    service.saySomethingElse(50);
    Assert.assertEquals(1, metric.testGetCount());
    service.saySomethingElse(150);
    Assert.assertEquals(2, metric.testGetCount());
    service.saySomethingElse(250);
    Assert.assertEquals(3, metric.testGetCount());

  }

}
