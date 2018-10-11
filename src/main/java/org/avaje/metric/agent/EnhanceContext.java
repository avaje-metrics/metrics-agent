package org.avaje.metric.agent;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Used to hold meta data, arguments and log levels for the enhancement.
 */
class EnhanceContext {

  private static final Logger logger = Logger.getLogger(EnhanceContext.class.getName());

  private final IgnoreClassHelper ignoreClassHelper;

  private final AgentManifest manifest;

  private final boolean readOnly;

  private final boolean enhanceSingleton;

  private final boolean includeStaticMethods;

  private PrintStream logout;

  private int logLevel;

  private Consumer<String> enhancementLogger;

  /**
   * Construct a context for enhancement.
   */
  EnhanceContext(AgentManifest manifest) {

    this.manifest = manifest;
    this.ignoreClassHelper = new IgnoreClassHelper(manifest.getPackages());
    this.logout = System.out;
    this.logLevel = manifest.getDebugLevel();

    this.includeStaticMethods = manifest.isIncludeStaticMethods();
    this.readOnly = manifest.isReadOnly();
    this.enhanceSingleton = manifest.isEnhanceSingleton();

    if (logLevel > 0) {
      log(8, "settings: debug[" + logLevel + "] readonly[" + readOnly + "]", "");
    }
  }

  /**
   * Return true if this class should be ignored. That is JDK classes and
   * known libraries JDBC drivers etc can be skipped.
   */
  boolean isIgnoreClass(String className) {
    if (className == null) {
      return true;
    }
    return ignoreClassHelper.isIgnoreClass(className);
  }

  /**
   * Change the logout to something other than system out.
   */
  void setLogout(PrintStream logout) {
    this.logout = logout;
  }

  /**
   * Log some debug output.
   */
  void log(int level, String msg, String extra) {
    if (logLevel >= level) {
      logout.println(msg + extra);
    }
  }

  void log(String className, String msg) {
    if (className != null) {
      msg = "cls: " + className + "  msg: " + msg;
    }
    logout.println("transform> " + msg);
  }

  boolean isLog(int level) {
    return logLevel >= level;
  }

  /**
   * Log an error.
   */
  void log(Throwable e) {
    e.printStackTrace(logout);
  }

  /**
   * Return the log level.
   */
  int getLogLevel() {
    return logLevel;
  }

  /**
   * Return true if this should go through the enhancement process but not
   * actually save the enhanced classes.
   * <p>
   * Set this to true to run through the enhancement process without actually
   * doing the enhancement for debugging etc.
   * </p>
   */
  boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Return true if classes annotated with Singleton should be enhanced.
   */
  boolean isEnhanceSingleton() {
    return enhanceSingleton;
  }

  /**
   * Return true if static methods should be included by default.
   */
  boolean isIncludeStaticMethods() {
    return includeStaticMethods;
  }

  boolean isIncludeRequestTiming() {
    return manifest.isIncludeRequestTiming();
  }

  boolean isNameIncludesPackage() {
    return manifest.isNameIncludesPackage();
  }

  boolean isIncludeJaxRS() {
    return manifest.isIncludeJaxRS();
  }

  boolean isIncludeSpring() {
    return manifest.isIncludeSpring();
  }

  void logAddingMetric(String mappedMetricName) {
    if (enhancementLogger != null) {
      enhancementLogger.accept(mappedMetricName);
    }
  }

  void setLogger(Consumer<String> logger) {
    this.enhancementLogger = logger;
  }
}
