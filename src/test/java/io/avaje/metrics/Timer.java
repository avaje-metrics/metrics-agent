package io.avaje.metrics;

/**
 * Test Double - copy of the real API TimedMetric.
 */
public interface Timer {

  void add(long startNanos);
  void addErr(long startNanos);
  Event startEvent();

  interface Event {
    void end();
    void endWithError();

    default void endWithError(Throwable error) {
      endWithError();
    }
  }
}
