package io.avaje.metrics.agent;

enum TimedSpanMode {
  DEFAULT,
  CHILD,
  ROOT,
  OFF;

  static TimedSpanMode of(String value) {
    if (value == null) {
      return DEFAULT;
    }
    return TimedSpanMode.valueOf(value);
  }
}
