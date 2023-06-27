package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockTimer;
import org.junit.Test;

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
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", Metrics.testLastMetricName());

    MockTimer metric = Metrics.testGetTimedMetric("web.api.CustomerResource.publicMethodWithJaxrs");
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
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", Metrics.testLastMetricName());
    assertTrue(Metrics.testLastMetricOpcodeSuccess());

    customerResource.nakedProtectedMethod();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", Metrics.testLastMetricName());

    customerResource.nakedPublicMethod();
    assertEquals("web.api.CustomerResource.nakedPublicMethod", Metrics.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", Metrics.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd");
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs1", Metrics.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd", 3);
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs2", Metrics.testLastMetricName());

    customerResource.findAll("ok");
    assertEquals("app.BaseResource.findAll", Metrics.testLastMetricName());

    customerResource.delete();
    assertEquals("app.BaseResource.delete", Metrics.testLastMetricName());

    customerResource.deleteX(23L, "as");// ();
    assertEquals("app.BaseResource.deleteX", Metrics.testLastMetricName());

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
      assertEquals("app.BaseResource.findAll", Metrics.testLastMetricName());
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
    assertEquals("web.api.CustomerResource.staticGeneral", Metrics.testLastMetricName());

  }
}
