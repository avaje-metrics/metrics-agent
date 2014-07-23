package org.avaje.metric.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects the annotation information.
 */
public class AnnotationInfo {

  /**
   * The Timed annotation.
   */
  private static final String ANNOTATION_TIMED = "Lorg/avaje/metric/annotation/Timed;";
  
  /**
   * The NotTimed annotation.
   */
  private static final String ANNOTATION_NOT_TIMED = "Lorg/avaje/metric/annotation/NotTimed;";

  /**
   * Set of JAXRS annotations we look for to detect web endpoints.
   */
  private static final Set<String> JAXRS_ANNOTATIONS = new HashSet<String>();
  static {
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/Path;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/HEAD;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/GET;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/PUT;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/POST;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/DELETE;");
    JAXRS_ANNOTATIONS.add("Ljavax/ws/rs/OPTIONS;");
  }

  /**
   * Return true if the annotation is the NotTimed annotation.
   */
  public static boolean isNotTimed(String desc) {
    return ANNOTATION_NOT_TIMED.equals(desc);
  }
  
  /**
   * Return true if the annotation is the Timed annotation.
   */
  public static boolean isTimed(String desc) {
    return ANNOTATION_TIMED.equals(desc);
  }
  
  /**
   * Return true if the annotation indicates a JAX-RS endpoint.
   */
  public static boolean isJaxrsEndpoint(String desc) {
    if (!desc.startsWith("Ljavax/ws/rs")) {
      return false;
    }
    return JAXRS_ANNOTATIONS.contains(desc);
  }  
  
  /**
   * The annotations read keyed by their description.
   */
	private final HashMap<String,Object> valueMap = new HashMap<String,Object>();
	
	private boolean containsJaxRs;
	
	public AnnotationInfo(){
	}

	/**
	 * Return true if the NotTimed annotation was collected.
	 */
	public boolean containsNotTimed() {
	  return valueMap.keySet().contains(ANNOTATION_NOT_TIMED);
	}
	
  /**
   * Return true if the Timed annotation was collected.
   */
  public boolean containsTimed() {
    return valueMap.keySet().contains(ANNOTATION_TIMED);
  }
  
  /**
   * Return true if a jaxrs annotation was detected.
   */
  public boolean containsJaxRs() {
    return containsJaxRs;
  }

	/**
	 * Add a annotation value.
	 */
	public void add(String name, Object value){
	  
	  valueMap.put(name, value);
	  if (isJaxrsEndpoint(name)) {
	    containsJaxRs = true;
    }
	}
  
  public String toString() {
    return valueMap.toString();
  }
}
