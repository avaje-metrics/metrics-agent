package org.avaje.metric;


public class MetricManager {

  public static TimedMetric getTimedMetric(String name) {
    System.out.println("================= getTimedMetric "+name);
    return new TimedMetric(name);
  }
}
