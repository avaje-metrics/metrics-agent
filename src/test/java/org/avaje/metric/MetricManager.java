package org.avaje.metric;


public class MetricManager {

  public static TimedMetric getTimedMetric(String name) {
    System.out.println("== MetricManager == getTimedMetric "+name);
    return new TimedMetric(name);
  }
}
