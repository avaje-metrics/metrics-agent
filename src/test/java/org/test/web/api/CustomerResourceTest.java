package org.test.web.api;

import junit.framework.Assert;

import org.avaje.metric.MockTimedMetric;
import org.avaje.metric.MetricManager;
import org.junit.Test;

public class CustomerResourceTest extends BaseTest {

  @Test
  public void testExecutionCount() {

    CustomerResource customerResource = new CustomerResource();

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());
    
    MockTimedMetric metric = MetricManager.testGetTimedMetric("web.api.CustomerResource.publicMethodWithJaxrs");
    metric.testReset();
    Assert.assertEquals(0, metric.testGetCount());

    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals(1, metric.testGetCount());

    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals(3, metric.testGetCount());

    customerResource.delete();
    customerResource.delete();

    
    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals(6, metric.testGetCount());

  }
  
  @Test
  public void testCustomerResource() {

    CustomerResource customerResource = new CustomerResource();

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    customerResource.publicMethodNotTimed();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());
    
    customerResource.protectedMethodWithJaxrs();
    Assert.assertEquals("web.api.CustomerResource.protectedMethodWithJaxrs", MetricManager.testLastMetricName());

    customerResource.nakedProtectedMethod();
    Assert.assertEquals("web.api.CustomerResource.protectedMethodWithJaxrs", MetricManager.testLastMetricName());

    customerResource.nakedPublicMethod();
    Assert.assertEquals("web.api.CustomerResource.nakedPublicMethod", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs();
    Assert.assertEquals("web.api.CustomerResource.publicMethodWithJaxrs", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd");
    Assert.assertEquals("web.api.CustomerResource.publicMethodWithJaxrs1", MetricManager.testLastMetricName());

    customerResource.publicMethodWithJaxrs("asd", 3);
    Assert.assertEquals("web.api.CustomerResource.publicMethodWithJaxrs2", MetricManager.testLastMetricName());
    
    customerResource.findAll("ok");
    Assert.assertEquals("web.api.BaseResource.findAll", MetricManager.testLastMetricName());
    
    customerResource.delete();
    Assert.assertEquals("web.api.BaseResource.delete", MetricManager.testLastMetricName());

    customerResource.deleteX(23L, "as");// ();
    Assert.assertEquals("web.api.BaseResource.deleteX", MetricManager.testLastMetricName());

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());

    customerResource.hashCode();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    customerResource.toString();
    Assert.assertNull(MetricManager.testLastMetricName());

    try {
     customerResource.findAll("throw");
     Assert.assertTrue("Never get here", false);
     
    } catch (IllegalArgumentException expected) {
      Assert.assertEquals("web.api.BaseResource.findAll", MetricManager.testLastMetricName());
      Assert.assertTrue(MetricManager.testLastMetricOpcodeError());
    }
  }
}
