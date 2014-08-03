package org.test.web.api;



import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Test;

public class CustomTimedResourceTest extends BaseTest {

 
  @Test
  public void test_methods() {

    CustomTimedResource resource = new CustomTimedResource();

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    resource.publicMethodNotTimed();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    resource.publicMethodNormal();
    Assert.assertEquals("web.api.CustomTimedResource.publicMethodNormal", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullName();
    Assert.assertEquals("myname.fully.defined", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithFullNameWhiteSpace();
    Assert.assertEquals("web.api.CustomTimedResource.publicMethodWithFullNameWhiteSpace", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithName();
    Assert.assertEquals("web.api.CustomTimedResource.someRandomName", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    resource.publicMethodWithNameWhiteSpace();
    Assert.assertEquals("web.api.CustomTimedResource.publicMethodWithNameWhiteSpace", MetricManager.testLastMetricName());
    Assert.assertTrue(MetricManager.testLastMetricOpcodeSuccess());

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());

    resource.hashCode();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    resource.toString();
    Assert.assertNull(MetricManager.testLastMetricName());

  }
  
  @Test
  public void testCustomerResource_staticMethods() {

    MetricManager.testReset();
    Assert.assertNull(MetricManager.testLastMetricName());
    
    CustomTimedResource.aStaticMethodNotAnnotated();
    Assert.assertNull(MetricManager.testLastMetricName());

    CustomTimedResource.aStaticMethodWithTimedAnnotation();
    Assert.assertEquals("web.api.CustomTimedResource.staticGeneral", MetricManager.testLastMetricName());

  }
}
