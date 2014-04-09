package org.avaje.metric.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to hold meta data, arguments and log levels for the enhancement.
 */
public class EnhanceContext {

	private static final Logger logger = Logger.getLogger(EnhanceContext.class.getName());

	private final ClassLoader classLoader;
	
	private final IgnoreClassHelper ignoreClassHelper;

	private final HashMap<String, String> agentArgsMap;

	private final boolean readOnly;
	
	private final boolean sysoutOnCollect;
	
	private final Map<String, String> nameMapping;

  private final String[] metricNameMatches;

	private PrintStream logout;

	private int logLevel;

	/**
	 * Construct a context for enhancement.
	 */
	public EnhanceContext(boolean subclassing, String agentArgs, ClassLoader classLoader) {

	  this.classLoader = classLoader;
		this.ignoreClassHelper = new IgnoreClassHelper(agentArgs);
		this.agentArgsMap = ArgParser.parse(agentArgs);

		this.logout = System.out;

		String debugValue = agentArgsMap.get("debug");
		if (debugValue != null) {
			try {
				logLevel = Integer.parseInt(debugValue);
			} catch (NumberFormatException e) {
				String msg = "Agent debug argument [" + debugValue+ "] is not an int?";
				logger.log(Level.WARNING, msg);
			}
		}     
		this.readOnly = getPropertyBoolean("readonly", false);
		this.sysoutOnCollect = getPropertyBoolean("sysoutoncollect", false);
		this.nameMapping = readNameMapping();
		log(1,"name mappings: "+nameMapping);
		log(1,"settings: debug["+debugValue+"] sysoutoncollect["+sysoutOnCollect+"] readonly["+readOnly+"]");
		this.metricNameMatches = getMetricNameMatches();
		System.out.println("match keys: "+Arrays.toString(this.metricNameMatches));
	}
	
	private String[] getMetricNameMatches() {
	  List<String> keys = new ArrayList<String>();
	  keys.addAll(this.nameMapping.keySet());
	  Collections.sort(keys);
	  System.out.println("KEYS: "+keys);
	  return keys.toArray(new String[keys.size()]);
	}

	private Enumeration<URL> getNameMappingResources() throws IOException {
	  if (classLoader != null) {
	    return classLoader.getResources("metric-name-mapping.txt");
	  } else {
      return getClass().getClassLoader().getResources("metric-name-mapping.txt");
	  }
	}
	
	private Map<String,String> readNameMapping() {
	  
	  Map<String,String> map = new HashMap<String, String>();
	  
	  try {
  	  Enumeration<URL> resources = getNameMappingResources();
  	  while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        InputStream inStream = url.openStream();
        try {
          Properties props = new Properties();
          props.load(inStream);
          
          Set<String> stringPropertyNames = props.stringPropertyNames();
          for (String propName : stringPropertyNames) {
            map.put(propName, props.getProperty(propName));
          }
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
	 * Return a value from the agent arguments using its key.
	 */
	public String getProperty(String key){
		return agentArgsMap.get(key.toLowerCase());
	}

	public boolean getPropertyBoolean(String key, boolean dflt){
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
	public boolean isIgnoreClass(String className) {
		return ignoreClassHelper.isIgnoreClass(className);
	}

	/**
	 * Change the logout to something other than system out.
	 */
	public void setLogout(PrintStream logout) {
		this.logout = logout;
	}

	/**
	 * Log some debug output.
	 */
	public void log(int level, String msg) {
		if (logLevel >= level) {
			logout.println(msg);
		}
	}
	
	public void log(String className, String msg) {
		if (className != null) {
			msg = "cls: " + className + "  msg: " + msg;
		}
		logout.println("transform> " + msg);
	}
	
	public boolean isLog(int level){
		return logLevel >= level;
	}

	/**
	 * Log an error.
	 */
	public void log(Throwable e) {
		e.printStackTrace(logout);
	}

	/**
	 * Return the log level.
	 */
	public int getLogLevel() {
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
	public boolean isReadOnly() {
		return readOnly;
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
   * Return a potentially cut down metric name.
   * <p>
   * For example, trim of extraneous package names or prefix controllers or
   * jaxrs endpoints with "web" etc.
   * </p>
   */
  public String getMappedName(String rawName) {
    for (int i = metricNameMatches.length-1; i >= 0; i--) {
      String name = metricNameMatches[i];
      if (rawName.startsWith(name)) {
        String prefix = nameMapping.get(name);
        if (prefix == null || prefix.length() == 0) {
          return trimMetricName(rawName.substring(name.length()));
          
        } else {
          return trimMetricName(prefix + rawName.substring(name.length()));
        }
      }
    }
    return rawName;
  }

  /**
   * Return true to add some debug sysout via the enhancement.
   */
  public boolean isSysoutOnCollect() {
    return sysoutOnCollect;
  }

}
