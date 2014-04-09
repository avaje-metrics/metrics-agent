package org.test.web.api;

import org.junit.Test;
import org.test.app.SimpleService;
import org.test.app.service.OrderService;

public class SimpleMainTest extends BaseTest {

  
  @Test
  public void testOrderService() {
    
    OrderService orderService = new OrderService();
    orderService.processOrders();
    orderService.processOrders();
    orderService.processOrders();
  }
  
  @Test
  public void testIndexResource() {
    
    IndexResource orderService = new IndexResource();//new SimpleService());
    //orderService.index();
  }
 
  
  @Test(expected=RuntimeException.class)
  public void testSimpleService() throws InterruptedException {
    
    SimpleService service = new SimpleService();
    service.toString();
    service.doSomething();
    service.doSomething();
    service.doSomething();
    service.doSomethingElse();
  }
}
