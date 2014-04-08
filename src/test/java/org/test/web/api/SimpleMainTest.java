package org.test.web.api;

import org.avaje.metric.agent.offline.MainTransform;
import org.test.app.SimpleService;

public class SimpleMainTest {

  public static void main(String[] args) throws InterruptedException {
    
    String[] transformArgs = {"./target/test-classes","org/test/**","debug=1;jaxrsProtected=true"};

   
    MainTransform.main(transformArgs);
    

    CustomerResource customerResource = new CustomerResource();
    customerResource.index();
    customerResource.findAll();

    SimpleService service = new SimpleService();
    service.toString();
    service.doSomething();

    service.doSomething();
    service.doSomethingElse();
    
    
    
  }
}
