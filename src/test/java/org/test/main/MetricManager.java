package org.test.main;

public class MetricManager {

  public static MetricCollector get(String name) {
    return new MetricCollector(name);
  }
}
