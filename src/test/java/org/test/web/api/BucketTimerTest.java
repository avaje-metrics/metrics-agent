package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockBucketTimer;
import org.junit.Test;
import org.test.app.OtherSimpleService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BucketTimerTest extends BaseTest {

  @Test
  public void testExecutionCount() throws InterruptedException {

    OtherSimpleService service = new OtherSimpleService();

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    service.sayHi();
    assertEquals("app.OtherSimpleService.sayHi", Metrics.testLastMetricName());

    MockBucketTimer metric = Metrics.testGetBucketTimedMetric("app.OtherSimpleService.saySomethingElse");
    metric.testReset();
    assertEquals(0, metric.testGetCount());

    service.saySomethingElse(50);
    assertEquals(1, metric.testGetCount());
    service.saySomethingElse(150);
    assertEquals(2, metric.testGetCount());
    service.saySomethingElse(250);
    assertEquals(3, metric.testGetCount());
  }

}
