package io.avaje.metrics;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface TimedMetric {

  void operationEnd(long startNanos);
  void operationEnd(long startNanos, boolean activeThreadContext);
  void operationErr(long startNanos);
  void operationErr(long startNanos, boolean activeThreadContext);

  boolean isActiveThreadContext();
}
