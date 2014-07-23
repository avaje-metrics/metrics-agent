package org.avaje.metric;

import java.util.HashMap;
import java.util.Map;

/**
 * Test double for the MetricManager service.
 */
public class MetricManager {

  private static Map<String, TimedMetric> cache = new HashMap<>();

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

  /**
   * For testing purpose get the TimedMetric if one has been created.
   */
  public synchronized static MockTimedMetric testGetTimedMetric(String name) {
    return (MockTimedMetric)cache.get(name);
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
