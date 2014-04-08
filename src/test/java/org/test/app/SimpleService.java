package org.test.app;

import org.avaje.metric.annotation.Timed;


@Timed
public class SimpleService {

  public void doSomething() throws RuntimeException {
    System.out.println("hello there");
  } 
  
  public void doSomethingElse() throws InterruptedException {
    try {
      internal();
      Thread.sleep(50);
      throw new NullPointerException("Intentional test of exception catch");
      
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }
  
  private void internal() {
    System.out.println("not very interesting");    
  } 
  
}
