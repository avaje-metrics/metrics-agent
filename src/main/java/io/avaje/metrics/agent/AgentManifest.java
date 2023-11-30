package io.avaje.metrics.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reads metrics manifest files to configure enhancement.
 */
public class AgentManifest {

  public static AgentManifest read(ClassLoader classLoader) {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    try {
      return new AgentManifest()
        .readManifests(classLoader, "metrics-common.mf")
        .readManifests(classLoader, "metrics.mf");

    } catch (IOException e) {
      // log to standard error and return empty
      System.err.println("Agent: error reading manifest resources");
      e.printStackTrace();
      return new AgentManifest();
    }
  }


  private final Set<String> includePackages = new HashSet<>();

  private boolean includeRequestTiming;

  private boolean includeSpringComponents;

  private boolean includeJaxRsComponents;

  private boolean nameIncludePackages;

  private int debugLevel;

  private boolean includeStaticMethods;
  private boolean enhanceSingleton = true;
  private boolean enhanceAvajeComponent = true;
  private boolean enhanceNonPrivate = false;

  private boolean readOnly;

  private final List<String> nameTrimPackages = new ArrayList<>();

  /**
   * Construct with no initial packages (to use with addRaw()).
   */
  public AgentManifest() {
  }

  public String toString() {
    return "packages:" + includePackages;
  }

  /**
   * Return the parsed set of packages that type query beans are in.
   */
  public Set<String> getPackages() {
    return includePackages;
  }


  /**
   * Return true if we should use automatically enhance JAX-RS endpoints.
   */
  public boolean isIncludeJaxRS() {
    return includeJaxRsComponents;
  }

  /**
   * Return true if we should use automatically enhance Spring components.
   */
  public boolean isIncludeSpring() {
    return includeSpringComponents;
  }

  public boolean isIncludeRequestTiming() {
    return includeRequestTiming;
  }

  /**
   * Read all the specific manifest files and return the set of packages containing type query beans.
   */
  AgentManifest readManifests(ClassLoader classLoader, String path) throws IOException {
    Enumeration<URL> resources = classLoader.getResources(path);
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      try {
        addResource(url.openStream());
      } catch (IOException e) {
        System.err.println("Error reading manifest resources " + url);
        e.printStackTrace();
      }
    }
    return this;
  }

  /**
   * Add given the manifest InputStream.
   */
  public void addResource(InputStream is) throws IOException {
    try {
      addManifest(new Manifest(is));
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        System.err.println("Error closing manifest resource");
        e.printStackTrace();
      }
    }
  }

  void readToggles(Attributes attributes) {
    String debug = attributes.getValue("debugLevel");
    if (debug != null) {
      debugLevel = Integer.parseInt(debug);
    }

    includeRequestTiming = bool(attributes, "requestTiming");
    includeStaticMethods = bool(attributes, "includeStaticMethods");
    enhanceSingleton = bool(attributes, "enhanceSingleton", enhanceSingleton);
    enhanceAvajeComponent = bool(attributes, "enhanceAvajeComponent", enhanceAvajeComponent);
    enhanceNonPrivate = bool(attributes, "enhanceNonPrivate", enhanceNonPrivate);
    readOnly = bool(attributes, "readOnly");
    includeSpringComponents = bool(attributes, "spring");
    includeJaxRsComponents = bool(attributes, "jaxrs");
    nameIncludePackages= bool(attributes, "nameIncludePackages");

    String trimPkgs = attributes.getValue("nameTrimPackages");
    if (trimPkgs != null) {
      String[] split = trimPkgs.split(",|;| ");
      for (String rawPkg : split) {
        rawPkg = rawPkg.trim();
        if (!rawPkg.isEmpty()) {
          nameTrimPackages.add(rawPkg + ".");
        }
      }
      nameTrimPackages.sort(Comparator.comparingInt(s -> s.length() * -1));
    }
  }

  private boolean bool(Attributes attributes, String key) {
    return bool(attributes, key, false);
  }

  private boolean bool(Attributes attributes, String key, boolean defaultValue) {
    String val = attributes.getValue(key);
    if (val == null) {
      return defaultValue;
    } else {
      return Boolean.parseBoolean(val.trim());
    }
  }


  private void addManifest(Manifest manifest) {
    Attributes attributes = manifest.getMainAttributes();
    readToggles(attributes);
    add(includePackages, attributes.getValue("packages"));
  }

  /**
   * Collect each individual package splitting by delimiters.
   */
  private void add(Set<String> addTo, String packages) {
    if (packages != null) {
      String[] split = packages.split(",|;| ");
      for (String aSplit : split) {
        String pkg = aSplit.trim();
        if (!pkg.isEmpty()) {
          addTo.add(pkg);
        }
      }
    }
  }

  /**
   * Set the debugLevel but only if it has not already been set (via metrics.mf file typically).
   */
  public AgentManifest setDefaultDebugLevel(int defaultDebugLevel) {
    if (debugLevel == 0) {
      debugLevel = defaultDebugLevel;
    }
    return this;
  }

  /**
   * Set the debugLevel.
   */
  public AgentManifest setDebugLevel(int debugLevel) {
    this.debugLevel = debugLevel;
    return this;
  }

  public AgentManifest setIncludeRequestTiming(boolean includeRequestTiming) {
    this.includeRequestTiming = includeRequestTiming;
    return this;
  }

  public AgentManifest setIncludeSpringComponents(boolean includeSpringComponents) {
    this.includeSpringComponents = includeSpringComponents;
    return this;
  }

  public AgentManifest setIncludeJaxRsComponents(boolean includeJaxRsComponents) {
    this.includeJaxRsComponents = includeJaxRsComponents;
    return this;
  }

  public AgentManifest setIncludeStaticMethods(boolean includeStaticMethods) {
    this.includeStaticMethods = includeStaticMethods;
    return this;
  }

  public int getDebugLevel() {
    return debugLevel;
  }

  public boolean isIncludeStaticMethods() {
    return includeStaticMethods;
  }

  public boolean isEnhanceSingleton() {
    return enhanceSingleton;
  }

  public boolean isEnhanceAvajeComponent() {
    return enhanceAvajeComponent;
  }

  public boolean isEnhanceNonPrivate() {
    return enhanceNonPrivate;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isNameIncludesPackage() {
    return nameIncludePackages;
  }

  String trim(String fullName) {
    for (String trimPackage : nameTrimPackages) {
      if (fullName.startsWith(trimPackage)) {
        return fullName.substring(trimPackage.length());
      }
    }
    return fullName;
  }
}
