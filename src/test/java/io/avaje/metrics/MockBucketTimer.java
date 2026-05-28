package io.avaje.metrics;

import java.util.Arrays;

/**
 * Test Double
 */
public class MockBucketTimer implements Timer {

  private final String name;
  private final String[] tags;
  private boolean traced;
  private boolean rootTraced;

  private int count;

  MockBucketTimer(String name, boolean traced) {
    this(name, new String[0], traced, false);
  }

  MockBucketTimer(String name, String[] tags, boolean traced) {
    this(name, tags, traced, false);
  }

  MockBucketTimer(String name, String[] tags, boolean traced, boolean rootTraced) {
    this.name = name;
    this.tags = Arrays.copyOf(tags, tags.length);
    this.traced = traced;
    this.rootTraced = rootTraced;
  }

  @Override
  public void add(long startNanos) {
    testOperationEnd(true, startNanos);
  }

  @Override
  public void addErr(long startNanos) {
    testOperationEnd(false, startNanos);
  }

  private void testOperationEnd(boolean success, long startNanos) {
    long exeNanos = System.nanoTime() - startNanos;
    System.out.println("... " + name + " operationEnd exe:" + exeNanos + " success:" + success);
    count++;
    Metrics.testOperationEnd(name, tags, success, success ? "add" : "addErr");
  }

  @Override
  public Event startEvent() {
    if (!traced) {
      throw new IllegalStateException("Timer not initialised for spans: " + name);
    }
    return new MockTimerEvent();
  }

  void testEnableTracing() {
    this.traced = true;
  }

  void testEnableRootTracing() {
    this.traced = true;
    this.rootTraced = true;
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

  public boolean testIsTraced() {
    return traced;
  }

  public boolean testIsRootTraced() {
    return rootTraced;
  }

  private final class MockTimerEvent implements Event {

    @Override
    public void end() {
      count++;
      Metrics.testOperationEnd(name, tags, true, "event.end");
    }

    @Override
    public void endWithError() {
      count++;
      Metrics.testOperationEnd(name, tags, false, "event.endWithError");
    }

    @Override
    public void endWithError(Throwable error) {
      count++;
      Metrics.testOperationEnd(name, tags, false, "event.endWithError", error);
    }
  }
}
