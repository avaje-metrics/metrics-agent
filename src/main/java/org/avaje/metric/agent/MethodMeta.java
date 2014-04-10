package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;

public class MethodMeta {

  private final int access;
  private final String name;
  private final String desc;
  private final String sig;
  private final String[] exceptions;
  
  private final AnnotationInfo annotationInfo;

  private String uniqueMethodName;
  
  public MethodMeta(int access, String name, String desc, String sig, String[] exceptions) {
    this.annotationInfo = new AnnotationInfo();
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.sig = sig;
    this.exceptions = exceptions;
  }

  public String toString() {
    return name + " " + desc+" access:"+access;
  }

  public boolean isProxyCandiate() {
    if (isPublicMethod()) {
      return !annotationInfo.containsNotTimed();
    }
    if (isProtectedMethod()) {
      return !annotationInfo.containsJaxRs();      
    }
    return false;
  }
  
  private boolean isPublicMethod() {
    return ((access & Opcodes.ACC_PUBLIC) != 0);
  }

  private boolean isProtectedMethod() {
    return ((access & Opcodes.ACC_PROTECTED) != 0);
  }

  public AnnotationInfo getAnnotationInfo() {
    return annotationInfo;
  }
  
  public MethodVisitor createMethodVisitor(ClassVisitor cw) {
    return cw.visitMethod(access, name, desc, sig, exceptions);
  }

  public int getAccess() {
    return access;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }

  public String getSig() {
    return sig;
  }

  public String[] getExceptions() {
    return exceptions;
  }

  public String getNameDescription() {
    return name+desc;
  }

  public void setUniqueMethodName(String uniqueMethodName) {
    this.uniqueMethodName = uniqueMethodName;
  }

  public String getUniqueMethodName() {
    return uniqueMethodName;
  }
  
}
