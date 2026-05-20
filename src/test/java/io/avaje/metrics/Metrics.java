package io.avaje.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test double for the MetricManager service.
 */
public class Metrics {

  private static final Map<String, Timer> cache = new HashMap<>();

  private static final Map<String, Timer> bucketCache = new HashMap<>();

  private static String lastMetricName;
  private static String[] lastMetricTags;

  private static int lastMetricOpcode;
  private static String lastOperationKind;
  private static Throwable lastThrowable;

  public static Timer timer(String name, String... tags) {
    return timer(name, false, tags);
  }

  public static Timer tracedTimer(String name, String... tags) {
    return timer(name, true, tags);
  }

  /**
   * Method called by the enhancement code.
   */
  public synchronized static Timer timer(String name) {
    Timer timer = cache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create timedMetric " + name);
      timer = new MockTimer(name, false);
      cache.put(key(name), timer);
    }
    return timer;
  }

  public synchronized static Timer tracedTimer(String name) {
    Timer timer = cache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create traced timedMetric " + name);
      timer = new MockTimer(name, true);
      cache.put(key(name), timer);
    } else {
      ((MockTimer) timer).testEnableTracing();
    }
    return timer;
  }

  private synchronized static Timer timer(String name, boolean traced, String... tags) {
    String key = key(name, tags);
    Timer timer = cache.get(key);
    if (timer == null) {
      System.out.println("== MetricManager: create " + (traced ? "traced " : "") + "timedMetric " + name + ":" + Arrays.toString(tags));
      timer = new MockTimer(name, tags, traced);
      cache.put(key, timer);
    } else if (traced) {
      ((MockTimer) timer).testEnableTracing();
    }
    return timer;
  }

  private static String key(String name) {
    return name;
  }

  private static String key(String name, String... tags) {
    return name + ":" + Arrays.toString(tags);
  }

  public synchronized static Timer timer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create BucketTimedMetric " + name);
      timer = new MockBucketTimer(name, false);
      bucketCache.put(key(name), timer);
    }
    return timer;
  }

  public synchronized static Timer tracedTimer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create traced BucketTimedMetric " + name);
      timer = new MockBucketTimer(name, true);
      bucketCache.put(key(name), timer);
    } else {
      ((MockBucketTimer) timer).testEnableTracing();
    }
    return timer;
  }

  /**
   * For testing purpose get the TimedMetric if one has been created.
   */
  public synchronized static MockTimer testGetTimedMetric(String name, String... tags) {
    return (MockTimer) cache.get(tags.length == 0 ? key(name) : key(name, tags));
  }

  public synchronized static MockBucketTimer testGetBucketTimedMetric(String name) {
    return (MockBucketTimer) bucketCache.get(key(name));
  }

  /**
   * Called when a timer ends so that we can assert the call occured.
   */
  protected static void testOperationEnd(String name, boolean success) {
    testOperationEnd(name, new String[0], success, "add");
  }

  protected static void testOperationEnd(String name, boolean success, String operationKind) {
    testOperationEnd(name, new String[0], success, operationKind, null);
  }

  protected static void testOperationEnd(String name, boolean success, String operationKind, Throwable throwable) {
    testOperationEnd(name, new String[0], success, operationKind, throwable);
  }

  protected static void testOperationEnd(String name, String[] tags, boolean success, String operationKind) {
    testOperationEnd(name, tags, success, operationKind, null);
  }

  protected static void testOperationEnd(String name, String[] tags, boolean success, String operationKind, Throwable throwable) {
    lastMetricName  = name;
    lastMetricTags = Arrays.copyOf(tags, tags.length);
    lastMetricOpcode = success ? 1 : 191;
    lastOperationKind = operationKind;
    lastThrowable = throwable;
  }

  public static String testLastMetricName() {
    return lastMetricName;
  }

  public static String[] testLastMetricTags() {
    return lastMetricTags == null ? null : Arrays.copyOf(lastMetricTags, lastMetricTags.length);
  }

  public static boolean testLastMetricOpcodeError() {
    return 191 == lastMetricOpcode;
  }

  public static int testLastMetricOpcode() {
    return lastMetricOpcode;
  }

  public static void testReset() {
    lastMetricName = null;
    lastMetricTags = null;
    lastMetricOpcode = 0;
    lastOperationKind = null;
    lastThrowable = null;
  }

  public static synchronized void testClear() {
    cache.clear();
    bucketCache.clear();
    testReset();
  }

  public static boolean testLastMetricOpcodeSuccess() {
    return 191 != lastMetricOpcode && 0 != lastMetricOpcode;
  }

  public static String testLastOperationKind() {
    return lastOperationKind;
  }

  public static boolean testLastOperationWasEvent() {
    return lastOperationKind != null && lastOperationKind.startsWith("event.");
  }

  public static boolean testLastOperationWasAdd() {
    return lastOperationKind != null && lastOperationKind.startsWith("add");
  }

  public static Throwable testLastThrowable() {
    return lastThrowable;
  }


}
