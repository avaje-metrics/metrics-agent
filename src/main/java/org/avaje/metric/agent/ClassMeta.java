package org.avaje.metric.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;

/**
 * Holds the meta data for an entity bean class that is being enhanced.
 */
public class ClassMeta {
	
	private int access;
	
	private String className;

	private String superClassName;

	private ClassMeta superMeta;

	private HashSet<String> classAnnotation = new HashSet<String>();

	private AnnotationInfo annotationInfo = new AnnotationInfo();

	private ArrayList<MethodMeta> methodMetaList = new ArrayList<MethodMeta>();

	private final EnhanceContext enhanceContext;
	
	public ClassMeta(EnhanceContext enhanceContext) {
		this.enhanceContext = enhanceContext;
	}
	
	/**
	 * Return the enhance context which has options for enhancement.
	 */
	public EnhanceContext getEnhanceContext() {
        return enhanceContext;
    }
	
    /**
	 * Return the class level annotations.
	 */
	public Set<String> getClassAnnotations() {
		return classAnnotation;
	}
	
	/**
	 * Return the AnnotationInfo collected on methods. 
	 * Used to determine Transactional method enhancement.
	 */
	public AnnotationInfo getAnnotationInfo() {
		return annotationInfo;
	}

	/**
	 * Return true if we should look at this super class and search for methods that we
	 * should proxy.
	 */
	public boolean isCheckForMethodsToProxy() {
	  return isAbstractClass();
	}

  private boolean isAbstractClass() {
    return ((access & Opcodes.ACC_ABSTRACT) != 0);
  }
  
	public String toString() {
		return className;
	}

	/**
	 * Return all the methods from this class and inherited that are 
	 * candidates for adding proxies for.
	 */
  public List<MethodMeta> getAllMethodMeta() {
    List<MethodMeta> list = new ArrayList<MethodMeta>();
    appendMethodMeta(list);
    return list;
  }

  /**
   * Gather the methods that are proxy candidates.
   */
	protected void appendMethodMeta(List<MethodMeta> list) {
	  for (int i = 0; i < methodMetaList.size(); i++) {
	    MethodMeta methodMeta = methodMetaList.get(i);
	    boolean proxyCandiate = methodMeta.isProxyCandiate();
	    log(1, "METHOD PROXY CANDIDATE? "+proxyCandiate, methodMeta.toString());
	    if (proxyCandiate) {
	      list.add(methodMeta);
	    }
    }
	  if (superMeta != null) {
	    superMeta.appendMethodMeta(list);
	  }
	}

	public void setClassName(int access, String className, String superClassName) {
	  this.access = access;
		this.className = className;
		this.superClassName = superClassName;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public boolean isLog(int level) {
		return enhanceContext.isLog(level);
	}

	public void log(int level, String msg, String extra) {
		if (enhanceContext.isLog(level)) {
	    if (className != null) {
	      msg = "cls: " + className + "  msg: " + msg;
	    }
	    enhanceContext.log(level, msg, extra);		  
		}
	}

	public ClassMeta getSuperMeta() {
		return superMeta;
	}

	public void setSuperMeta(ClassMeta superMeta) {
		this.superMeta = superMeta;
	}

	/**
	 * Return the className of this entity class.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Add a class annotation.
	 */
	public void addClassAnnotation(String desc) {
		classAnnotation.add(desc);
	}


	public MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, String desc, String sig, String[] ex) {

		MethodMeta methodMeta = new MethodMeta(access, name, desc, sig, ex);
		methodMetaList.add(methodMeta);

		return new MethodMetaReader(mv, methodMeta);
	}


	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		appendDescription(sb);
		return sb.toString();
	}

	private void appendDescription(StringBuilder sb) {
		sb.append(className);
		if (superMeta != null) {
			sb.append(" : ");
			superMeta.appendDescription(sb);
		}
	}

  private static final class MethodMetaReader extends MethodVisitor {
    
    final MethodMeta methodMeta;

    MethodMetaReader(MethodVisitor mv, MethodMeta methodMeta) {
      super(Opcodes.ASM4, mv);
      this.methodMeta = methodMeta;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return new AnnotationInfoVisitor(methodMeta.getAnnotationInfo());
    }
  }
}
