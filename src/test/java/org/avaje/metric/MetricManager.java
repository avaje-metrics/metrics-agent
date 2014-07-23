package org.avaje.metric;

import java.util.HashMap;
import java.util.Map;

/**
 * Test double for the MetricManager service.
 */
public class MetricManager {

  private static Map<String,TimedMetric> cache = new HashMap<>();  
    
  /**
   * Method called by the enhancement code. 
   */
  public synchronized static TimedMetric getTimedMetric(String name) {
      
    
    TimedMetric timedMetric = cache.get(name);
    if (timedMetric == null) {
        System.out.println("== MetricManager: create timedMetric "+name);
        timedMetric = new DefaultTimedMetric(name);
        cache.put(name, timedMetric);
    }
    
    System.out.println("== MetricManager: return timedMetric "+name);
    return timedMetric;
  }
  
  /**
   * For testing purpose get the TimedMetric if one has been created.
   */
  protected synchronized static TimedMetric getForTesting(String name) {
      return cache.get(name);
  }
}
