package org.test.main;

import org.avaje.metric.annotation.Timed;
import org.test.main.MetricCollector;
import org.test.main.MetricManager;

@Timed
public class ExampleService {

  public static long timer;
  
  private static MetricCollector _$metric_1 = MetricManager.get("simpleSerivce.doSomething");
  private static MetricCollector _$metric_2 = MetricManager.get("simpleSerivce.doSomethingElse");
  
  public void doSomething() throws InterruptedException {
//    long _$metricStart = System.nanoTime();
//    try {
      System.out.println("123");
      //Thread.sleep(100);
//    } finally {
//      _$metric_1.end(System.nanoTime() - _$metricStart);
//    }
  }
  
  public void doSomethingElse(int opCode) {
    long start = System.nanoTime();
    System.out.println("not very interesting");
    //int opCode = 123;
    _$metric_2.end(System.nanoTime() - start, opCode == 177);
  }
}
