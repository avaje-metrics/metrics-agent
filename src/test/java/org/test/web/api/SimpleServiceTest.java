package org.test.web.api;

import org.junit.Test;
import org.test.app.SimpleService;

public class SimpleServiceTest extends BaseTest {

  @Test(expected = RuntimeException.class)
  public void testSimpleServiceThatThrowsException() throws InterruptedException {

    SimpleService service = new SimpleService();
    service.doSomething();
    service.doSomething();
    service.doSomething();
    service.doSomethingElse();
  }
}
