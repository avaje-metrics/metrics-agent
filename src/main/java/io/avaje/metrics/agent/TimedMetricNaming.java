package io.avaje.metrics.agent;

enum TimedMetricNaming {
  FULL_NAME,
  LABEL_TAG;

  static TimedMetricNaming of(String value) {
    if (value == null || value.trim().isEmpty()) {
      return LABEL_TAG;
    }
    switch (value.trim()) {
      case "full-name":
        return FULL_NAME;
      case "label-tag":
      default:
        return LABEL_TAG;
    }
  }
}
