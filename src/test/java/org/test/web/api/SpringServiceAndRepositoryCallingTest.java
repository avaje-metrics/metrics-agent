package org.test.web.api;

import org.avaje.metric.MockTimedMetric;
import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Test;
import org.test.app.model.Contact;
import org.test.app.service.ContactDataLayer;
import org.test.app.service.ContactRepository;
import org.test.app.service.ContactServiceImpl;

/**
 * Test a simple service and repository annotated with spring annotations.
 */
public class SpringServiceAndRepositoryCallingTest extends BaseTest {

  @Test
  public void test() throws InterruptedException {

    ContactDataLayer contactDataLayer = new ContactRepository();
    ContactServiceImpl impl = new ContactServiceImpl(contactDataLayer);

    MockTimedMetric alertMetric = MetricManager.testGetTimedMetric("app.service.ContactServiceImpl.sendAlert");
    alertMetric.testReset();

    MockTimedMetric repoMetric = MetricManager.testGetTimedMetric("app.service.ContactRepository.fetchData");
    repoMetric.testReset();

    
    impl.sendAlert(new Contact());
    Assert.assertEquals("app.service.ContactServiceImpl.sendAlert", MetricManager.testLastMetricName());
    Assert.assertEquals(1, alertMetric.testGetCount());
    Assert.assertEquals(1, repoMetric.testGetCount());

    impl.sendAlert(null);
    Assert.assertEquals(2, alertMetric.testGetCount());
    Assert.assertEquals(2, repoMetric.testGetCount());

    impl.sendAlert(null);
    contactDataLayer.fetchData();
    contactDataLayer.fetchData();
    
    Assert.assertEquals(3, alertMetric.testGetCount());
    Assert.assertEquals(5, repoMetric.testGetCount());

  }
}
