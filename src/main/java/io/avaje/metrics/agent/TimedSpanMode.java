package io.avaje.metrics.agent;

enum TimedSpanMode {
  DEFAULT,
  ON,
  OFF;

  static TimedSpanMode of(String value) {
    if (value == null) {
      return DEFAULT;
    }
    return TimedSpanMode.valueOf(value);
  }
}
