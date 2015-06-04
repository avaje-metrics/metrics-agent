package org.avaje.metric;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface BucketTimedMetric {

  /**
   * Method called by the enhanced code.
   */
  void operationEnd(int opCode, long startNanos, boolean requestTiming);

  boolean isActiveThreadContext();

}