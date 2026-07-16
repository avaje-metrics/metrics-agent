package org.test.web.api;



import io.avaje.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
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
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomTimedResource.publicMethodNormal"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullName();
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:myname.fully.defined"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullNameWhiteSpace();
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomTimedResource.publicMethodWithFullNameWhiteSpace"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithName();
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:someRandomName"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithNameWhiteSpace();
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomTimedResource.publicMethodWithNameWhiteSpace"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    resource.publicMethodWithMethodPrefix();
    assertEquals("lambda", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomTimedResource.publicMethodWithMethodPrefix"}, Metrics.testLastMetricTags());
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
    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:staticGeneral"}, Metrics.testLastMetricTags());

  }
}
