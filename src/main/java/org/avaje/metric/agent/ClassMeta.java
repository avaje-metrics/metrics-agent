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

	//private static final Logger logger = Logger.getLogger(ClassMeta.class.getName());

	//private static final String OBJECT_CLASS = Object.class.getName().replace('.', '/');
	
	private int access;
	
	private String className;

	private String superClassName;

	private ClassMeta superMeta;

//	private HashSet<String> existingMethods = new HashSet<String>();
//
//	private HashSet<String> existingSuperMethods = new HashSet<String>();

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

//	/**
//	 * Return the transactional annotation information for a matching interface method.
//	 */
//	public AnnotationInfo getInterfaceTransactionalInfo(String methodName, String methodDesc) {
//
//		AnnotationInfo annotationInfo = null;
//
//		for (int i = 0; i < methodMetaList.size(); i++) {
//			MethodMeta meta = methodMetaList.get(i);
//			if (meta.isMatch(methodName, methodDesc)) {
//				if (annotationInfo != null) {
//					String msg = "Error in [" + className + "] searching the transactional methods[" + methodMetaList
//							+ "] found more than one match for the transactional method:" + methodName + " "
//							+ methodDesc;
//					
//					logger.log(Level.SEVERE, msg);
//					log(msg);
//					
//				} else {
//					annotationInfo = meta.getAnnotationInfo();
//					if (isLog(9)){
//						log("... found transactional info from interface "+className+" "+methodName+" "+methodDesc);
//					}
//				}
//			}
//		}
//
//		return annotationInfo;
//	}

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
	    log("METHOD PROXY CANDIDATE? "+proxyCandiate+"  "+methodMeta);
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

	public void log(String msg) {
		if (className != null) {
			msg = "cls: " + className + "  msg: " + msg;
		}
		enhanceContext.log(1,"transform> " + msg);
	}

	public ClassMeta getSuperMeta() {
		return superMeta;
	}

	public void setSuperMeta(ClassMeta superMeta) {
		this.superMeta = superMeta;
	}

//  /**
//   * Return true if the class has an Entity, Embeddable, MappedSuperclass or
//   * LdapDomain annotation.
//   */
//  public boolean isEntity() {
//    if (classAnnotation.contains(EnhanceConstants.ENTITY_ANNOTATION)) {
//      return true;
//    }
//    if (classAnnotation.contains(EnhanceConstants.EMBEDDABLE_ANNOTATION)) {
//      return true;
//    }
//    if (classAnnotation.contains(EnhanceConstants.MAPPEDSUPERCLASS_ANNOTATION)) {
//      return true;
//    }
//    if (classAnnotation.contains(EnhanceConstants.LDAPDOMAIN_ANNOTATION)) {
//      return true;
//    }
//    return false;
//  }

//	/**
//	 * Return true for classes not already enhanced and yet annotated with entity, embeddable or mappedSuperclass.
//	 */
//	public boolean isEntityEnhancementRequired() {
//		if (alreadyEnhanced) {
//			return false;
//		}
//		if (isEntity()){
//			return true;
//		}
//		return false;
//	}

	/**
	 * Return the className of this entity class.
	 */
	public String getClassName() {
		return className;
	}

//	/**
//	 * Return true if this entity bean has a super class that is an entity.
//	 */
//	public boolean isSuperClassEntity() {
//		if (superMeta == null) {
//			return false;
//		} else {
//			return superMeta.isEntity();
//		}
//	}

	/**
	 * Add a class annotation.
	 */
	public void addClassAnnotation(String desc) {
		classAnnotation.add(desc);
	}

//	/**
//	 * Only for subclassing, add known methods on the original entity class.
//	 * <p>
//	 * Used to check that the methods exist. They may not in special cases such
//	 * as entity beans that use a finder etc with read only properties.
//	 * </p>
//	 */
//	public void addExistingSuperMethod(String methodName, String methodDesc) {
//		existingSuperMethods.add(methodName + methodDesc);
//	}
//
//	/**
//	 * Add an existing method.
//	 */
//	public void addExistingMethod(String methodName, String methodDesc) {
//		existingMethods.add(methodName + methodDesc);
//	}
//
//	/**
//	 * Return true if the method already exists on the bean.
//	 */
//	public boolean isExistingMethod(String methodName, String methodDesc) {
//		return existingMethods.contains(methodName + methodDesc);
//	}
//	
//	/**
//	 * Only for subclassing return true if the method exists on the original
//	 * entity class.
//	 */
//	public boolean isExistingSuperMethod(String methodName, String methodDesc) {
//		return existingSuperMethods.contains(methodName + methodDesc);
//	}

	public MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, String desc, String sig, String[] ex) {

		MethodMeta methodMeta = new MethodMeta(access, name, desc, sig, ex);
		methodMetaList.add(methodMeta);

		return new MethodMetaReader(mv, methodMeta);
	}

	private static final class MethodMetaReader extends MethodVisitor {
		//final MethodVisitor mv;
		final MethodMeta methodMeta;

		MethodMetaReader(MethodVisitor mv, MethodMeta methodMeta) {
		  super(Opcodes.ASM4, mv);
			//this.mv = mv;
			this.methodMeta = methodMeta;
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return new AnnotationInfoVisitor(methodMeta.getAnnotationInfo());
		}
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

	
}
