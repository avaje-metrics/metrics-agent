package org.avaje.metric;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface BucketTimedMetric {

  /**
   * Method called by the enhanced code.
   */
  //public void operationEnd(long nanos, int opCode);
  public void operationEnd(int opCode, long startNanos);

  //public void addEventSince(boolean success, long startNanos);
  
}