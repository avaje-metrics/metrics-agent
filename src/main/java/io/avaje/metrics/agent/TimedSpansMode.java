package io.avaje.metrics.agent;

enum TimedSpansMode {
  DEFAULT_OFF,
  DEFAULT_ON,
  DISABLED;

  static TimedSpansMode of(String value) {
    if (value == null || value.trim().isEmpty()) {
      return DEFAULT_OFF;
    }
    switch (value.trim()) {
      case "default-on":
        return DEFAULT_ON;
      case "disabled":
        return DISABLED;
      case "default-off":
      default:
        return DEFAULT_OFF;
    }
  }

  boolean resolve(TimedSpanMode classMode, TimedSpanMode methodMode) {
    if (this == DISABLED) {
      return false;
    }
    TimedSpanMode effective = methodMode != TimedSpanMode.DEFAULT ? methodMode : classMode;
    if (effective == TimedSpanMode.DEFAULT) {
      return this == DEFAULT_ON;
    }
    return effective == TimedSpanMode.ON;
  }
}
