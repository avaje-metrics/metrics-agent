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
    return timer(name, false, false, tags);
  }

  public static Timer tracedTimer(String name, String... tags) {
    return timer(name, true, false, tags);
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

  public synchronized static Timer rootTracedTimer(String name) {
    Timer timer = cache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create root timedMetric " + name);
      timer = new MockTimer(name, new String[0], true, true);
      cache.put(key(name), timer);
    } else {
      ((MockTimer) timer).testEnableRootTracing();
    }
    return timer;
  }

  private synchronized static Timer timer(String name, boolean traced, boolean rootTraced, String... tags) {
    String key = key(name, tags);
    Timer timer = cache.get(key);
    if (timer == null) {
      System.out.println("== MetricManager: create " + (rootTraced ? "root " : traced ? "traced " : "") + "timedMetric " + name + ":" + Arrays.toString(tags));
      timer = new MockTimer(name, tags, traced, rootTraced);
      cache.put(key, timer);
    } else if (rootTraced) {
      ((MockTimer) timer).testEnableRootTracing();
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

  public static TimerBuilder timerBuilder(String name) {
    return new TestTimerBuilder(name);
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

  public synchronized static Timer rootTracedTimer(String name, int... bucketRanges) {

    Timer timer = bucketCache.get(key(name));
    if (timer == null) {
      System.out.println("== MetricManager: create root BucketTimedMetric " + name);
      timer = new MockBucketTimer(name, new String[0], true, true);
      bucketCache.put(key(name), timer);
    } else {
      ((MockBucketTimer) timer).testEnableRootTracing();
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
    return testGetBucketTimedMetric(name, new String[0]);
  }

  public synchronized static MockBucketTimer testGetBucketTimedMetric(String name, String... tags) {
    return (MockBucketTimer) bucketCache.get(tags.length == 0 ? key(name) : key(name, tags));
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

  private synchronized static Timer timer(String name, boolean traced, boolean rootTraced, int[] bucketRanges, String... tags) {
    if (bucketRanges == null || bucketRanges.length == 0) {
      if (tags.length == 0) {
        if (rootTraced) {
          return rootTracedTimer(name);
        }
        return traced ? tracedTimer(name) : timer(name);
      }
      return timer(name, traced, rootTraced, tags);
    }
    if (tags.length == 0) {
      if (rootTraced) {
        return rootTracedTimer(name, bucketRanges);
      }
      return traced ? tracedTimer(name, bucketRanges) : timer(name, bucketRanges);
    }
    String key = key(name, tags);
    Timer timer = bucketCache.get(key);
    if (timer == null) {
      System.out.println("== MetricManager: create " + (rootTraced ? "root " : traced ? "traced " : "") + "BucketTimedMetric " + name + ":" + Arrays.toString(tags));
      timer = new MockBucketTimer(name, tags, traced, rootTraced);
      bucketCache.put(key, timer);
    } else if (rootTraced) {
      ((MockBucketTimer) timer).testEnableRootTracing();
    } else if (traced) {
      ((MockBucketTimer) timer).testEnableTracing();
    }
    return timer;
  }

  private static final class TestTimerBuilder implements TimerBuilder {

    private final String name;
    private String[] tags = new String[0];
    private int[] bucketRanges = new int[0];

    private TestTimerBuilder(String name) {
      this.name = name;
    }

    @Override
    public TimerBuilder tags(Tags tags) {
      this.tags = tags.array();
      return this;
    }

    @Override
    public TimerBuilder bucketRanges(int... bucketRangesMillis) {
      this.bucketRanges = Arrays.copyOf(bucketRangesMillis, bucketRangesMillis.length);
      return this;
    }

    @Override
    public Timer build() {
      return timer(name, false, false, bucketRanges, tags);
    }

    @Override
    public Timer buildTraced() {
      return timer(name, true, false, bucketRanges, tags);
    }

    @Override
    public Timer buildRootTraced() {
      return timer(name, true, true, bucketRanges, tags);
    }
  }

}
