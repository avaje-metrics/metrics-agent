package org.test.main;

import org.test.app.SimpleService;

public class SimpleMainTest {

  public static void main(String[] args) throws InterruptedException {
    
    SimpleService service = new SimpleService();
    service.toString();
    service.doSomething();

    service.doSomething();
    service.doSomethingElse();
    
    
  }
}
