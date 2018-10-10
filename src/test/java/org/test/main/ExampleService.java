package org.test.main;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.annotation.NotTimed;

@NotTimed
public class ExampleService extends BaseService {

  private static TimedMetric _$metric_1;
  private static TimedMetric _$metric_2;
  private static TimedMetric _$metric_3;

  static {
    _$initMetrics();
  }

  private static void _$initMetrics() {
    _$metric_1 = MetricManager.getTimedMetric("simpleSerivce.doSomething");
    _$metric_2 = MetricManager.getTimedMetric("simpleSerivce.doSomethingElse");
    _$metric_3 = MetricManager.getTimedMetric("simpleSerivce.doBucketSomething", 100, 200);
  }

  public void doSomething() throws InterruptedException {
    long _$metricStart = System.nanoTime();
    try {
      System.out.println("123");
      // Thread.sleep(100);
      int opCode = 100;
      _$metric_1.operationEnd(opCode, _$metricStart, false);
    } catch (RuntimeException e) {
      //int opCode = 191;
      //_$metric_1.addEventSince(opCode != 191, _$metricStart);
      _$metric_1.operationEnd(191, _$metricStart, false);
    }
  }


  public void doBucketSomething() throws InterruptedException {
    long _$metricStart = System.nanoTime();
    try {
      System.out.println("123");
      // Thread.sleep(100);
      int opCode = 100;
      _$metric_3.operationEnd(opCode, _$metricStart, false);
    } catch (RuntimeException e) {
      //int opCode = 191;
      //_$metric_1.addEventSince(opCode != 191, _$metricStart);
      _$metric_3.operationEnd(191, _$metricStart, false);
    }
  }

  public void doSomethingElse(int opCode) {
    long start = System.nanoTime();
    System.out.println("not very interesting");
    // int opCode = 123;
    //_$metric_2.operationEnd(System.nanoTime() - start, opCode);
    _$metric_2.operationEnd(opCode, start, false);
  }

  // public String findAll(String orderBy) {
  // return super.findAll(orderBy);
  // }

  public String delete() {
    return super.delete();
  }
}
