package org.avaje.metric.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Name mapping used to provide shorter metric names with common prefixes.
 */
public class NameMapping {

  private static final String METRIC_NAME_MAPPING_RESOURCE = "metric-name-mapping.txt";
  private static final String MATCH_INCLUDE = "match.include.";
  private static final String MATCH_EXCLUDE = "match.exclude.";

  private static final String MATCH = "match.";
  private static final String BUCKETS = ".buckets";

  private final ClassLoader classLoader;

  private final Map<String, String> nameMapping;

  private final String[] metricNameMatches;

  private final List<Match> matchIncludes = new ArrayList<>();

  private final List<Match> matches = new ArrayList<>();

  /**
   * Create with a classLoader.
   * <p>
   * The classLoader is used for loading metric-name-mapping.txt resources (if any).
   * </p>
   */
  NameMapping(ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.nameMapping = readNameMapping();
    this.metricNameMatches = getMetricNameMatches();
  }

  public String toString() {
    return nameMapping.toString();
  }

  String getMatches() {
    return Arrays.toString(this.metricNameMatches);
  }

  /**
   * Return a potentially cut down metric name.
   * <p>
   * For example, trim of extraneous package names or prefix controllers or
   * JAX-RS endpoints with "web" etc.
   * </p>
   */
  String getMappedName(String rawName) {

    // search for a match in reverse order
    for (int i = metricNameMatches.length-1; i >= 0; i--) {
      String name = metricNameMatches[i];
      if (rawName.startsWith(name)) {
        String prefix = nameMapping.get(name);
        if (prefix == null || prefix.length() == 0) {
          // trim without any prefix
          return trimMetricName(rawName.substring(name.length()));

        } else {
          return trimMetricName(prefix + rawName.substring(name.length()));
        }
      }
    }
    return rawName;
  }

  /**
   * trim off any leading period.
   */
  private String trimMetricName(String metricName) {
    if (metricName.startsWith(".")) {
      return metricName.substring(1);
    }
    return metricName;
  }

  /**
   * Return the keys in order.
   */
  private String[] getMetricNameMatches() {

    List<String> keys = new ArrayList<>();
    keys.addAll(this.nameMapping.keySet());
    Collections.sort(keys);
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Return all the metric-name-mapping.txt resources (if any).
   */
  private Enumeration<URL> getNameMappingResources() throws IOException {

    if (classLoader != null) {
      return classLoader.getResources(METRIC_NAME_MAPPING_RESOURCE);
    } else {
      return getClass().getClassLoader().getResources(METRIC_NAME_MAPPING_RESOURCE);
    }
  }

  /**
   * Return all the mappings as a Map.
   */
  private Map<String,String> readNameMapping() {

    Map<String,String> map = new HashMap<>();

    try {
      Enumeration<URL> resources = getNameMappingResources();
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        InputStream inStream = url.openStream();
        try {
          loadAsProperties(map, inStream);
        } finally {
          if (inStream != null) {
            inStream.close();
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error trying to read metric-name-mapping.properties resources");
      e.printStackTrace();
    }
    return map;
  }

  /**
   * Load an external name mapping file.
   */
  void loadFile(String fileName) {

    File file = new File(fileName.trim());
    if (!file.exists()) {
      System.err.println("Unable to load from file["+fileName+"] - file does not exist");
    }
    try {
      FileInputStream is = new FileInputStream(file);
      try {
        loadAsProperties(nameMapping, is);
      } catch (IOException e) {
        System.err.println("Error trying to process file ["+fileName+"]");
        e.printStackTrace();
      } finally {
        is.close();
      }
    } catch (IOException e) {
      System.err.println("Error trying to process file ["+fileName+"]");
      e.printStackTrace();
    }
  }

  private void loadAsProperties(Map<String, String> map, InputStream inStream) throws IOException {

    Properties props = new Properties();
    props.load(inStream);

    Set<String> stringPropertyNames = props.stringPropertyNames();
    for (String propName : stringPropertyNames) {
      String value = props.getProperty(propName);
      if (propName.startsWith(MATCH)) {
        addMatcher(propName, value, props);
      } else {
        map.put(propName, value);
      }
    }
  }

  /**
   * Add a matcher in the form of match.id=... and match.id.buckets=... or match.exclude.id=...
   */
  private void addMatcher(String propName, String pattern, Properties props) {

    if (propName.startsWith(MATCH_INCLUDE)) {
      matchIncludes.add(new Match(pattern));

    } else if (propName.startsWith(MATCH_EXCLUDE)) {
      matches.add(new Match(pattern));

    } else if (!propName.endsWith(BUCKETS)) {
      String bucketKey = propName + BUCKETS;
      String buckets = props.getProperty(bucketKey);
      matches.add(new Match(pattern, parseBuckets(buckets)));
    }
  }

  private int[] parseBuckets(String buckets) {
    if (buckets == null) {
      return null;
    }
    try {
      String[] split = buckets.split(",");
      int[] values = new int[split.length];
      for (int i = 0; i < split.length; i++) {
        values[i] = Integer.parseInt(split[i].trim());
      }
      return values;

    } catch (RuntimeException e) {
      // send to sys err as we don't know if logging is setup during enhancement
      System.err.println("Error in org.avaje.metric.agent.NameMapping parsing buckets ["+buckets+"]");
      e.printStackTrace();

      // we will carry on without buckets specified
      return null;
    }
  }

  List<Match> getPatternMatch() {
    return matches;
  }

  /**
   * Return true if at least match.include is specified.
   */
  boolean hasMatchIncludes() {
    return !matchIncludes.isEmpty();
  }

  /**
   * Return true if the class matches our list of includes.
   */
  boolean matchIncludeClass(String className) {
    for (Match matchInclude : matchIncludes) {
      if (matchInclude.like.matches(className)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if the class matches our list of includes.
   */
  boolean matchExcludeMethod(String classNameAndMethod) {
    for (Match match : matches) {
      if (match.like.matches(classNameAndMethod)) {
        return !match.include;
      }
    }
    return false;
  }

  Match findMatch(String className) {

    for (Match matche : matches) {
      if (matche.like.matches(className)) {
        return matche;
      }
    }
    return null;
  }


  public static class Match {

    final boolean include;

    final LikeMatcher like;

    final String pattern;

    final int[] buckets;

    Match(String pattern) {
      this.include = false;
      this.pattern = pattern;
      this.like = new LikeMatcher(pattern);
      this.buckets = null;
    }

    Match(String pattern, int[] buckets) {
      this.include = true;
      this.pattern = pattern;
      this.like = new LikeMatcher(pattern);
      this.buckets = buckets;
    }

    public String toString() {
      return "include:" + include + " pattern:" + pattern;
    }
  }
}
