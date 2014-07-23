package org.avaje.metric;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface TimedMetric {

  /**
   * Method called by the enhanced code.
   */
  public void operationEnd(long nanos, int opCode);

}