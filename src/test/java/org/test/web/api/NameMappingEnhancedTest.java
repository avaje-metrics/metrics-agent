package org.test.web.api;

import com.myapp.something.nice.ExcludeMeService;
import com.myapp.something.nice.NiceService;
import com.myapp.something.controller.NiceController;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockBucketTimer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NameMappingEnhancedTest extends BaseTest {

  @Test
  public void testSuccessExecution() {

    NiceService niceService = new NiceService();

    MockBucketTimer metric = Metrics.testGetBucketTimedMetric("app.NiceService.doNice");
    metric.testReset();
    Metrics.testReset();

    assertEquals(0, metric.testGetCount());

    niceService.doNice();
    assertEquals(1, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    niceService.doNice();
    assertEquals(2, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    niceService.doNice();
    assertEquals(3, metric.testGetCount());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

  }


  @Test
  public void testExclude() {

    ExcludeMeService excludeMeService = new ExcludeMeService();

    excludeMeService.excludeMe();
    excludeMeService.excludeMe();
    excludeMeService.excludeMe();
    excludeMeService.excludeMe();

  }

  @Test
  public void testController() {

    NiceController controller = new NiceController();

    controller.doStuff();
    controller.doStuff();
    controller.doStuff();
    controller.doStuff();
  }
}
