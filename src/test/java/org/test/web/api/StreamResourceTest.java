package org.test.web.api;

import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StreamResourceTest extends BaseTest {

  @Test
  public void testStreamMethodUsesStreamPrefix() {

    StreamResource resource = new StreamResource();

    Metrics.testReset();

    resource.streamAll();
    assertEquals("web.stream.StreamResource.streamAll", Metrics.testLastMetricName());
  }

  @Test
  public void testNonStreamMethodUsesApiPrefix() {

    StreamResource resource = new StreamResource();

    Metrics.testReset();

    resource.findOne();
    assertEquals("web.api.StreamResource.findOne", Metrics.testLastMetricName());
  }

  @Test
  public void testPublicStreamMethodUsesStreamPrefix() {

    StreamResource resource = new StreamResource();

    Metrics.testReset();

    resource.publicStreamMethod();
    assertEquals("web.stream.StreamResource.publicStreamMethod", Metrics.testLastMetricName());
  }

  @Test
  public void testPublicNonStreamMethodUsesApiPrefix() {

    StreamResource resource = new StreamResource();

    Metrics.testReset();

    resource.publicNormalMethod();
    assertEquals("web.api.StreamResource.publicNormalMethod", Metrics.testLastMetricName());
  }
}
