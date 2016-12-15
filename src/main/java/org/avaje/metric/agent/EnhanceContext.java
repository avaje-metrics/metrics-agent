package org.avaje.metric.agent;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to hold meta data, arguments and log levels for the enhancement.
 */
class EnhanceContext {

  private static final Logger logger = Logger.getLogger(EnhanceContext.class.getName());

	private final IgnoreClassHelper ignoreClassHelper;

	private final HashMap<String, String> agentArgsMap;

	private final boolean readOnly;

	private final boolean enhanceSingleton;

  private final boolean includeStaticMethods;

	private PrintStream logout;

	private int logLevel;

	private final NameMapping nameMapping;

	/**
	 * Construct a context for enhancement.
	 */
	EnhanceContext(String agentArgs, ClassLoader classLoader, Map<String, String> properties) {

 		this.agentArgsMap = ArgParser.parse(agentArgs);
		this.ignoreClassHelper = new IgnoreClassHelper(splitPackages(agentArgsMap.get("packages")));
		this.nameMapping = new NameMapping(classLoader, properties);
 		this.logout = System.out;

		String debugValue = agentArgsMap.get("debug");
 		if (debugValue != null) {
			try {
				logLevel = Integer.parseInt(debugValue.trim());
			} catch (NumberFormatException e) {
				String msg = "Avaje metrics agent debug argument [" + debugValue+ "] is not an int? ignoring.";
        System.err.println(msg);
				logger.log(Level.WARNING, msg);
			}
		}

    this.includeStaticMethods = getPropertyBoolean("includestaticmethods", false);
    this.readOnly = getPropertyBoolean("readonly", false);
		this.enhanceSingleton = getPropertyBoolean("enhancesingleton", true);

		if (logLevel > 0) {
      log(8, "settings: debug["+debugValue+"] readonly["+readOnly+"]", "");
  		log(1, "name mappings: ", nameMapping.toString());
      log(1, "match patterns: ", nameMapping.getPatternMatch().toString());
      log(1, "match keys: ", nameMapping.getMatches());
		}
	}

	private Collection<String> splitPackages(String packages) {
		return packages != null ? Arrays.asList(packages.split(",")) : null;
	}

	/**
	 * Return a potentially cut down metric name.
	 * <p>
	 * For example, trim of extraneous package names or prefix controllers or
	 * JAX-RS endpoints with "web" etc.
	 * </p>
	 */
	String getMappedName(String rawName) {
		return nameMapping.getMappedName(rawName);
	}

	/**
	 * Return a value from the agent arguments using its key.
	 */
	private String getProperty(String key){
		return agentArgsMap.get(key.toLowerCase());
	}

	private boolean getPropertyBoolean(String key, boolean dflt){
		String s = getProperty(key);
		if (s == null){
			return dflt;
		} else {
			return s.trim().equalsIgnoreCase("true");
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
    if (nameMapping.hasMatchIncludes()) {
			className = className.replace('/', '.');
			return !nameMapping.matchIncludeClass(className);
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
	
	boolean isLog(int level){
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
   * Return a Match object for the metric. Can be used to assign
   * buckets or explicitly exclude.
   */
  NameMapping.Match findMatch(String metricFullName) {
    return nameMapping.findMatch(metricFullName);
  }

  /**
   * Return true if static methods should be included by default.
   */
  boolean isIncludeStaticMethods() {
    return includeStaticMethods;
  }

	boolean isMatchExcludeMethod(String className, String method) {
		return nameMapping.matchExcludeMethod(className+"."+method);
	}
}
