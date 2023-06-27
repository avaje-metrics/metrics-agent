package org.test.web.api;



import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomTimedResourceTest extends BaseTest {


  @Test
  public void test_methods() {

    CustomTimedResource resource = new CustomTimedResource();

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    resource.publicMethodNotTimed();
    assertNull(Metrics.testLastMetricName());

    resource.publicMethodNormal();
    assertEquals("myapi.CustomTimedResource.publicMethodNormal", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullName();
    assertEquals("myname.fully.defined", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullNameWhiteSpace();
    assertEquals("myapi.CustomTimedResource.publicMethodWithFullNameWhiteSpace", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithName();
    assertEquals("myapi.CustomTimedResource.someRandomName", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithNameWhiteSpace();
    assertEquals("myapi.CustomTimedResource.publicMethodWithNameWhiteSpace", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    resource.hashCode();
    assertNull(Metrics.testLastMetricName());

    resource.toString();
    assertNull(Metrics.testLastMetricName());

  }

  @Test
  public void testCustomerResource_staticMethods() {

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    CustomTimedResource.aStaticMethodNotAnnotated();
    assertNull(Metrics.testLastMetricName());

    CustomTimedResource.aStaticMethodWithTimedAnnotation();
    assertEquals("myapi.CustomTimedResource.staticGeneral", Metrics.testLastMetricName());

  }
}
