package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockTimer;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CustomerResourceTest extends BaseTest {

  @Test
  public void testExecutionCount() {

    CustomerResource customerResource = new CustomerResource();

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs"}, Metrics.testLastMetricTags());

    MockTimer metric = Metrics.testGetTimedMetric("web.api", "label:CustomerResource.publicMethodWithJaxrs");
    metric.testReset();
    assertEquals(0, metric.testGetCount());

    customerResource.publicMethodWithJaxrs();
    assertEquals(1, metric.testGetCount());

    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    assertEquals(3, metric.testGetCount());

    customerResource.delete();
    customerResource.delete();


    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    assertEquals(6, metric.testGetCount());

  }

  @Test
  public void testCustomerResource() {

    CustomerResource customerResource = new CustomerResource();

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    customerResource.publicMethodNotTimed();
    assertNull(Metrics.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    customerResource.nakedProtectedMethod();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs"}, Metrics.testLastMetricTags());

    customerResource.nakedPublicMethod();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.nakedPublicMethod"}, Metrics.testLastMetricTags());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs"}, Metrics.testLastMetricTags());

    customerResource.publicMethodWithJaxrs("asd");
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs1"}, Metrics.testLastMetricTags());

    customerResource.publicMethodWithJaxrs("asd", 3);
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:CustomerResource.publicMethodWithJaxrs2"}, Metrics.testLastMetricTags());

    customerResource.findAll("ok");
    assertEquals("app.component", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:BaseResource.findAll"}, Metrics.testLastMetricTags());

    customerResource.delete();
    assertEquals("app.component", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:BaseResource.delete"}, Metrics.testLastMetricTags());

    customerResource.deleteX(23L, "as");// ();
    assertEquals("app.component", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:BaseResource.deleteX"}, Metrics.testLastMetricTags());

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    customerResource.hashCode();
    assertNull(Metrics.testLastMetricName());

    customerResource.toString();
    assertNull(Metrics.testLastMetricName());

    try {
      customerResource.findAll("throw");
      fail("Never get here");

    } catch (IllegalArgumentException expected) {
      assertEquals("app.component", Metrics.testLastMetricName());
      assertArrayEquals(new String[]{"label:BaseResource.findAll"}, Metrics.testLastMetricTags());
      assertTrue(Metrics.testLastMetricOpcodeError());
    }
  }

  @Test
  public void testCustomerResource_staticMethods() {

    Metrics.testReset();
    assertNull(Metrics.testLastMetricName());

    CustomerResource.aStaticMethodNotAnnotated();
    assertNull(Metrics.testLastMetricName());

    CustomerResource.aStaticMethodWithTimedAnnotation();
    assertEquals("web.api", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:staticGeneral"}, Metrics.testLastMetricTags());

  }
}
