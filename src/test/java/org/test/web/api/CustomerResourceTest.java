package org.test.web.api;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MockTimedMetric;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CustomerResourceTest extends BaseTest {

  @Test
  public void testExecutionCount() {

    CustomerResource customerResource = new CustomerResource();

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());

    MockTimedMetric metric = MetricManager.testGetTimedMetric("web.api.CustomerResource.publicMethodWithJaxrs");
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

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    customerResource.publicMethodNotTimed();
    assertNull(MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());
    assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    customerResource.nakedProtectedMethod();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());

    customerResource.nakedPublicMethod();
    assertEquals("web.api.CustomerResource.nakedPublicMethod", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd");
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs1", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd", 3);
    assertEquals("web.api.CustomerResource.publicMethodWithJaxrs2", MetricManager.testLastMetricName());

    customerResource.findAll("ok");
    assertEquals("app.BaseResource.findAll", MetricManager.testLastMetricName());

    customerResource.delete();
    assertEquals("app.BaseResource.delete", MetricManager.testLastMetricName());

    customerResource.deleteX(23L, "as");// ();
    assertEquals("app.BaseResource.deleteX", MetricManager.testLastMetricName());

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    customerResource.hashCode();
    assertNull(MetricManager.testLastMetricName());

    customerResource.toString();
    assertNull(MetricManager.testLastMetricName());

    try {
      customerResource.findAll("throw");
      fail("Never get here");

    } catch (IllegalArgumentException expected) {
      assertEquals("app.BaseResource.findAll", MetricManager.testLastMetricName());
      assertTrue(MetricManager.testLastMetricOpcodeError());
    }
  }

  @Test
  public void testCustomerResource_staticMethods() {

    MetricManager.testReset();
    assertNull(MetricManager.testLastMetricName());

    CustomerResource.aStaticMethodNotAnnotated();
    assertNull(MetricManager.testLastMetricName());

    CustomerResource.aStaticMethodWithTimedAnnotation();
    assertEquals("web.api.CustomerResource.staticGeneral", MetricManager.testLastMetricName());

  }
}
