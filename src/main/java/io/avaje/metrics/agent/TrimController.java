package io.avaje.metrics.agent;

final class TrimController {

  static String trim(String shortClassName) {
    if (shortClassName.endsWith("Controller")) {
      return trimTrailing(shortClassName, 10);
    } else if (shortClassName.endsWith("Resource")) {
      return trimTrailing(shortClassName, 8);
    }
    return shortClassName;
  }

  private static String trimTrailing(String shortClassName, int len) {
    int end = shortClassName.length() - len;
    return shortClassName.substring(0, end);
  }

}
