package org.test.main;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.Timer;
import io.avaje.metrics.annotation.NotTimed;

@NotTimed
public class ExampleService extends BaseService {

  private static Timer _$metric_1;
  private static Timer _$metric_2;
  private static Timer _$metric_3;

  static {
    _$initMetrics();
  }

  private static void _$initMetrics() {
    _$metric_1 = Metrics.timer("simpleSerivce.doSomething");
    _$metric_2 = Metrics.timer("simpleSerivce.doSomethingElse");
    _$metric_3 = Metrics.timer("simpleSerivce.doBucketSomething", 100, 200);
  }

  public void doSomething() throws InterruptedException {
    long _$metricStart = System.nanoTime();
    try {
      System.out.println("123");
      // Thread.sleep(100);
      _$metric_1.add(_$metricStart);
    } catch (RuntimeException e) {
      _$metric_1.addErr(_$metricStart);
    }
  }


  public void doBucketSomething() throws InterruptedException {
    long _$metricStart = System.nanoTime();
    try {
      System.out.println("123");
      // Thread.sleep(100);
      _$metric_3.add(_$metricStart);
    } catch (RuntimeException e) {
      _$metric_3.addErr(_$metricStart);
    }
  }

  public void doSomethingElse(int opCode) {
    long start = System.nanoTime();
    System.out.println("not very interesting");
    // int opCode = 123;
    //_$metric_2.operationEnd(System.nanoTime() - start, opCode);
    _$metric_2.add(start);
  }

  // public String findAll(String orderBy) {
  // return super.findAll(orderBy);
  // }

  public String delete() {
    return super.delete();
  }
}
