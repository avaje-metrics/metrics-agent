package io.avaje.metrics;

/**
 * Test Double
 */
public class MockBucketTimer implements Timer {

  private final String name;
  private boolean traced;

  private int count;

  MockBucketTimer(String name, boolean traced) {
    this.name = name;
    this.traced = traced;
  }

  @Override
  public void add(long startNanos) {
    testOperationEnd(true, startNanos, false);
  }

  @Override
  public void add(long startNanos, boolean activeThreadContext) {
    testOperationEnd(true, startNanos, activeThreadContext);
  }

  @Override
  public void addErr(long startNanos) {
    testOperationEnd(false, startNanos, false);
  }

  @Override
  public void addErr(long startNanos, boolean activeThreadContext) {
    testOperationEnd(false, startNanos, activeThreadContext);
  }

  private void testOperationEnd(boolean success, long startNanos, boolean activeThreadContext) {
    long exeNanos = System.nanoTime() - startNanos;
    System.out.println("... " + name + " operationEnd exe:" + exeNanos + " success:" + success + " activeThreadContext:" + activeThreadContext);
    count++;
    Metrics.testOperationEnd(name, success, activeThreadContext, success ? "add" : "addErr");
  }

  @Override
  public boolean isRequestTiming() {
    return true;
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

  private final class MockTimerEvent implements Event {

    @Override
    public void end() {
      count++;
      Metrics.testOperationEnd(name, true, false, "event.end");
    }

    @Override
    public void endWithError() {
      count++;
      Metrics.testOperationEnd(name, false, false, "event.endWithError");
    }

    @Override
    public void endWithError(Throwable error) {
      count++;
      Metrics.testOperationEnd(name, false, false, "event.endWithError", error);
    }
  }
}
