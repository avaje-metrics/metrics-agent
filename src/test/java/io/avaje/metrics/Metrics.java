package io.avaje.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Test double for the MetricManager service.
 */
public class Metrics {

  private static Map<String, Timer> cache = new HashMap<>();

  private static Map<String, Timer> bucketCache = new HashMap<>();

  private static String lastMetricName;

  private static int lastMetricOpcode;

  private static boolean lastActiveThreadContext;
  private static String lastOperationKind;
  private static Throwable lastThrowable;

  /**
   * Method called by the enhancement code.
   */
  public synchronized static Timer timer(String name) {

    Timer timer = cache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create timedMetric " + name);
      timer = new MockTimer(name, false);
      cache.put(name, timer);
    }
    return timer;
  }

  public synchronized static Timer tracedTimer(String name) {

    Timer timer = cache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create traced timedMetric " + name);
      timer = new MockTimer(name, true);
      cache.put(name, timer);
    } else {
      ((MockTimer) timer).testEnableTracing();
    }
    return timer;
  }


  public synchronized static Timer timer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create BucketTimedMetric " + name);
      timer = new MockBucketTimer(name, false);
      bucketCache.put(name, timer);
    }
    return timer;
  }

  public synchronized static Timer tracedTimer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create traced BucketTimedMetric " + name);
      timer = new MockBucketTimer(name, true);
      bucketCache.put(name, timer);
    } else {
      ((MockBucketTimer) timer).testEnableTracing();
    }
    return timer;
  }

  /**
   * For testing purpose get the TimedMetric if one has been created.
   */
  public synchronized static MockTimer testGetTimedMetric(String name) {
    return (MockTimer)cache.get(name);
  }

  public synchronized static MockBucketTimer testGetBucketTimedMetric(String name) {
    return (MockBucketTimer)bucketCache.get(name);
  }

  /**
   * Called when a timer ends so that we can assert the call occured.
   */
  protected static void testOperationEnd(String name, boolean success, boolean activeThreadContext) {
    testOperationEnd(name, success, activeThreadContext, "add");
  }

  protected static void testOperationEnd(String name, boolean success, boolean activeThreadContext, String operationKind) {
    testOperationEnd(name, success, activeThreadContext, operationKind, null);
  }

  protected static void testOperationEnd(String name, boolean success, boolean activeThreadContext, String operationKind, Throwable throwable) {
    lastMetricName  = name;
    lastMetricOpcode = success ? 1 : 191;
    lastActiveThreadContext = activeThreadContext;
    lastOperationKind = operationKind;
    lastThrowable = throwable;
  }

  public static String testLastMetricName() {
    return lastMetricName;
  }

  public static boolean testLastMetricOpcodeError() {
    return 191 == lastMetricOpcode;
  }

  public static boolean testLastActiveThreadContext() {
    return lastActiveThreadContext;
  }

  public static int testLastMetricOpcode() {
    return lastMetricOpcode;
  }

  public static void testReset() {
    lastMetricName = null;
    lastMetricOpcode = 0;
    lastActiveThreadContext = false;
    lastOperationKind = null;
    lastThrowable = null;
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
