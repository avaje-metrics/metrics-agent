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
  public void operationEnd(int opCode, long startNanos) {
    System.out.println("not using request timing ...");
    operationEnd(opCode, startNanos, false);
  }

  @Override
  public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {
    long exeNanos = System.nanoTime() - startNanos;
    System.out.println("... " + name + " operationEnd exe:" + exeNanos + " opCode:" + opCode + " activeThreadContext:" + activeThreadContext);
    count++;
    MetricManager.operationEnd(name, opCode, activeThreadContext);
  }

  public boolean isActiveThreadContext() {
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
