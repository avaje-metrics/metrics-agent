package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockTimer;
import org.junit.Test;
import org.test.app.model.Contact;
import org.test.app.service.ContactDataLayer;
import org.test.app.service.ContactRepository;
import org.test.app.service.ContactServiceImpl;

import static org.junit.Assert.assertEquals;

/**
 * Test a simple service and repository annotated with spring annotations.
 */
public class SpringServiceAndRepositoryCallingTest extends BaseTest {

  @Test
  public void test() {

    ContactDataLayer contactDataLayer = new ContactRepository();
    ContactServiceImpl impl = new ContactServiceImpl(contactDataLayer);

    MockTimer alertMetric = Metrics.testGetTimedMetric("service.ContactServiceImpl.sendAlert");
    alertMetric.testReset();

    MockTimer repoMetric = Metrics.testGetTimedMetric("repo.ContactRepository.fetchData");
    repoMetric.testReset();


    impl.sendAlert(new Contact());
    assertEquals("service.ContactServiceImpl.sendAlert", Metrics.testLastMetricName());
    assertEquals(1, alertMetric.testGetCount());
    assertEquals(1, repoMetric.testGetCount());

    impl.sendAlert(null);
    assertEquals(2, alertMetric.testGetCount());
    assertEquals(2, repoMetric.testGetCount());

    impl.sendAlert(null);
    contactDataLayer.fetchData();
    contactDataLayer.fetchData();

    assertEquals(3, alertMetric.testGetCount());
    assertEquals(5, repoMetric.testGetCount());

  }
}
