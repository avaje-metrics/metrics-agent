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

  /**
   * Method called by the enhancement code.
   */
  public synchronized static Timer timer(String name) {

    Timer timer = cache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create timedMetric " + name);
      timer = new MockTimer(name);
      cache.put(name, timer);
    }
    return timer;
  }


  public synchronized static Timer timer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(name);
    if (timer == null) {
      System.out.println("== MetricManager: create BucketTimedMetric " + name);
      timer = new MockBucketTimer(name);
      bucketCache.put(name, timer);
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
    lastMetricName  = name;
    lastMetricOpcode = success ? 1 : 191;
    lastActiveThreadContext = activeThreadContext;
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
  }

  public static boolean testLastMetricOpcodeSuccess() {
    return 191 != lastMetricOpcode && 0 != lastMetricOpcode;
  }


}
