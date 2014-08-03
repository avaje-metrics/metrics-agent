package org.avaje.metric;

import java.util.HashMap;
import java.util.Map;

/**
 * Test double for the MetricManager service.
 */
public class MetricManager {

  private static Map<String, TimedMetric> cache = new HashMap<>();
  
  private static Map<String, BucketTimedMetric> bucketCache = new HashMap<>();

  private static String lastMetricName;
  
  private static int lastMetricOpcode;
  
  /**
   * Method called by the enhancement code.
   */
  public synchronized static TimedMetric getTimedMetric(String name) {

    TimedMetric timedMetric = cache.get(name);
    if (timedMetric == null) {
      System.out.println("== MetricManager: create timedMetric " + name);
      timedMetric = new MockTimedMetric(name);
      cache.put(name, timedMetric);
    }

    System.out.println("== MetricManager: return timedMetric " + name);
    return timedMetric;
  }


  public synchronized static BucketTimedMetric getTimedMetric(String name, int... bucketRanges) {
    
    BucketTimedMetric timedMetric = bucketCache.get(name);
    if (timedMetric == null) {
      System.out.println("== MetricManager: create BucketTimedMetric " + name);
      timedMetric = new MockBucketTimedMetric(name);
      bucketCache.put(name, timedMetric);
    }

    System.out.println("== MetricManager: return BucketTimedMetric " + name);
    return timedMetric;
  }
  
  /**
   * For testing purpose get the TimedMetric if one has been created.
   */
  public synchronized static MockTimedMetric testGetTimedMetric(String name) {
    return (MockTimedMetric)cache.get(name);
  }

  public synchronized static MockBucketTimedMetric testGetBucketTimedMetric(String name) {
    return (MockBucketTimedMetric)bucketCache.get(name);
  }
  
  /**
   * Called when a timer ends so that we can assert the call occured.
   */
  protected static void operationEnd(String name, int opCode) {
    lastMetricName  = name;
    lastMetricOpcode = opCode;
  }
  
  public static String testLastMetricName() {
    return lastMetricName;
  }
  
  public static boolean testLastMetricOpcodeError() {
    return 191 == lastMetricOpcode;
  }

  
  public static int testLastMetricOpcode() {
    return lastMetricOpcode;
  }

  public static void testReset() {
    lastMetricName = null;
    lastMetricOpcode = 0;
  }

  public static boolean testLastMetricOpcodeSuccess() {
    return 191 != lastMetricOpcode && 0 != lastMetricOpcode;
  }

  
}
