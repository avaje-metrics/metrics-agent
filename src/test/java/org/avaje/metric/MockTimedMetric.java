package org.avaje.metric;

/**
 * Test Double
 */
public class MockTimedMetric implements TimedMetric {

  private final String name;

  private int count;
  
  public MockTimedMetric(String name) {
    this.name = name;
  }

  @Override
  public void operationEnd(long nanos, int opCode) {
    System.out.println("... " + name + " operationEnd exe:" + nanos + " opCode:" + opCode);
    count++;
    MetricManager.operationEnd(name, opCode);
  }

  /**
   * Return the count for the metric.
   */
  public int testGetCount() {
    return count;
  }

  /**
   * Reset count back to 0.
   */
  public void testReset() {
    count = 0;
  }
}
