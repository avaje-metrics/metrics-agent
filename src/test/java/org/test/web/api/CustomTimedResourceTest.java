package org.test.web.api;



import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomTimedResourceTest extends BaseTest {


  @Test
  public void test_methods() {

    CustomTimedResource resource = new CustomTimedResource();

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    resource.publicMethodNotTimed();
    assertNull(MetricManager.testLastMetricName());

    resource.publicMethodNormal();
    assertEquals("myapi.CustomTimedResource.publicMethodNormal", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullName();
    assertEquals("myname.fully.defined", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullNameWhiteSpace();
    assertEquals("myapi.CustomTimedResource.publicMethodWithFullNameWhiteSpace", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithName();
    assertEquals("myapi.CustomTimedResource.someRandomName", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithNameWhiteSpace();
    assertEquals("myapi.CustomTimedResource.publicMethodWithNameWhiteSpace", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    resource.hashCode();
    assertNull(MetricManager.testLastMetricName());

    resource.toString();
    assertNull(MetricManager.testLastMetricName());

  }

  @Test
  public void testCustomerResource_staticMethods() {

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    CustomTimedResource.aStaticMethodNotAnnotated();
    assertNull(MetricManager.testLastMetricName());

    CustomTimedResource.aStaticMethodWithTimedAnnotation();
    assertEquals("myapi.CustomTimedResource.staticGeneral", MetricManager.testLastMetricName());

  }
}
