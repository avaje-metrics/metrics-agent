package io.avaje.metrics.agent;

enum TimedMetricNaming {
  FULL_NAME,
  LABEL_TAG;

  static TimedMetricNaming of(String value) {
    if (value == null || value.trim().isEmpty()) {
      return FULL_NAME;
    }
    switch (value.trim()) {
      case "label-tag":
        return LABEL_TAG;
      case "full-name":
      default:
        return FULL_NAME;
    }
  }
}
