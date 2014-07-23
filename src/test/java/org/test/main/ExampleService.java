package org.test.main;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.annotation.NotTimed;

@NotTimed
public class ExampleService extends BaseService {

  private static TimedMetric _$metric_1;// =
                                        // MetricManager.getTimedMetric("simpleSerivce.doSomething");
  private static TimedMetric _$metric_2;// =
                                        // MetricManager.getTimedMetric("simpleSerivce.doSomethingElse");

  static {
    _$initMetrics();
  }

  private static void _$initMetrics() {
    _$metric_1 = MetricManager.getTimedMetric("simpleSerivce.doSomething");
    _$metric_2 = MetricManager.getTimedMetric("simpleSerivce.doSomethingElse");
  }

  public void doSomething() throws InterruptedException {
    long _$metricStart = System.nanoTime();
    try {
      System.out.println("123");
      // Thread.sleep(100);
    } finally {
      _$metric_1.operationEnd(System.nanoTime() - _$metricStart, 100);
    }
  }

  public void doSomethingElse(int opCode) {
    long start = System.nanoTime();
    System.out.println("not very interesting");
    // int opCode = 123;
    _$metric_2.operationEnd(System.nanoTime() - start, opCode);
  }

  // public String findAll(String orderBy) {
  // return super.findAll(orderBy);
  // }

  public String delete() {
    return super.delete();
  }
}
