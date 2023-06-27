package io.avaje.metrics;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface Timer {

  void add(long startNanos);
  void add(long startNanos, boolean activeThreadContext);
  void addErr(long startNanos);
  void addErr(long startNanos, boolean activeThreadContext);

  boolean isRequestTiming();
}
