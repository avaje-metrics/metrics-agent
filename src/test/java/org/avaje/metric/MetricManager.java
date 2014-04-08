package org.avaje.metric;


public class MetricManager {

  public static TimedMetric getTimedMetric(String name) {
    return new TimedMetric(name);
  }
}
