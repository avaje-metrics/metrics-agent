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
  public void operationEnd(int opCode, long startNanos, boolean requestTiming) {
    long exeNanos = System.nanoTime() - startNanos;
    System.out.println("... " + name + " operationEnd exe:" + exeNanos + " opCode:" + opCode + " requestTiming:" + requestTiming);
    count++;
    MetricManager.operationEnd(name, opCode, requestTiming);
  }

  public boolean isRequestTiming() {
    return true;
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
