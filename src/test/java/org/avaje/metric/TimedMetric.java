package org.avaje.metric;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface TimedMetric {

  /**
   * Method called by the enhanced code.
   */
  void operationEnd(int opCode, long startNanos, boolean activeThreadContext);

  void operationEnd(int opCode, long startNanos);

  boolean isActiveThreadContext();
}
