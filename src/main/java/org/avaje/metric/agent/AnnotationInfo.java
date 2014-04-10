package org.avaje.metric.agent;

import java.util.HashMap;
import java.util.Set;

/**
 * Collects the annotation information.
 */
public class AnnotationInfo {

  private static final String ANNOTATION_TIMED = "Lorg/avaje/metric/annotation/Timed;";
  private static final String ANNOTATION_NOT_TIMED = "Lorg/avaje/metric/annotation/NotTimed;";

  public static boolean isNotTimed(String desc) {
    return ANNOTATION_NOT_TIMED.equals(desc);
  }
  
  public static boolean isTimed(String desc) {
    return ANNOTATION_TIMED.equals(desc);
  }
  
  public static boolean isJaxrsEndpoint(String desc) {
    if (!desc.startsWith("Ljavax/ws/rs")) {
      return false;
    }
    return desc.equals("Ljavax/ws/rs/Path;") || desc.equals("Ljavax/ws/rs/GET;") 
        || desc.equals("Ljavax/ws/rs/PUT;") || desc.equals("Ljavax/ws/rs/POST;") 
        || desc.equals("Ljavax/ws/rs/DELETE;") || desc.equals("Ljavax/ws/rs/OPTIONS;") 
        || desc.equals("Ljavax/ws/rs/HEAD;");
  }  
  
	private final HashMap<String,Object> valueMap = new HashMap<String,Object>();

	//private AnnotationInfo parent;
	
	/**
	 * The parent is typically the class level annotation information
	 * which could be considered to hold default values.
	 */
	public AnnotationInfo(){
		//this.parent = parent;
	}
	
	public boolean containsNotTimed() {
	  return valueMap.keySet().contains(ANNOTATION_NOT_TIMED);
	}
	
  public boolean containsTimed() {
    return valueMap.keySet().contains(ANNOTATION_TIMED);
  }
  
  public boolean containsJaxRs() {
    Set<String> keySet = valueMap.keySet();
    for (String desc : keySet) {
      if (isJaxrsEndpoint(desc)) {
        return true;
      }
    }
    return false;
  }
	
	public String toString() {
		return valueMap.toString();
	}
	
//	
//	public AnnotationInfo getParent() {
//		return parent;
//	}
//
//	public void setParent(AnnotationInfo parent) {
//		this.parent = parent;
//	}

	/**
	 * Add a annotation value.
	 */
	public void add(String name, Object value){
	  valueMap.put(name, value);
	}

//	/**
//	 * Add a enum annotation value.
//	 */
//	public void addEnum(String prefix, String name, String desc, String value){
//		
//		add(prefix, name, value);
//	}
//	
//	private String getKey(String prefix, String name){
//		if (prefix == null){
//			return name;
//		} else {
//			return prefix+"."+name;
//		}
//	}
//	
//	/**
//	 * Return a value out of the map.
//	 */
//	public Object getValue(String key){
//		Object o = valueMap.get(key);
//		if (o == null && parent != null){
//			// try getting value from parent
//			o = parent.getValue(key);
//		}
//		return o;
//	}
}
