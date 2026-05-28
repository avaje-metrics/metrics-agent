package io.avaje.metrics.agent;

enum TimedSpansMode {
  DEFAULT_OFF,
  DEFAULT_CHILD,
  DISABLED;

  static TimedSpansMode of(String value) {
    if (value == null || value.trim().isEmpty()) {
      return DEFAULT_OFF;
    }
    switch (value.trim()) {
      case "default-child":
        return DEFAULT_CHILD;
      case "disabled":
        return DISABLED;
      case "default-off":
        return DEFAULT_OFF;
      default:
        throw new IllegalArgumentException("Invalid timedSpans mode " + value);
    }
  }

  TimedSpanMode resolve(TimedSpanMode classMode, TimedSpanMode methodMode) {
    if (this == DISABLED) {
      return TimedSpanMode.OFF;
    }
    TimedSpanMode effective = methodMode != TimedSpanMode.DEFAULT ? methodMode : classMode;
    if (effective == TimedSpanMode.DEFAULT) {
      return this == DEFAULT_CHILD ? TimedSpanMode.CHILD : TimedSpanMode.OFF;
    }
    return effective;
  }
}
