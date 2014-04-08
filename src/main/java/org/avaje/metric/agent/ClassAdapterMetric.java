package org.avaje.metric.agent;

import java.util.ArrayList;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.FieldVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;

/**
 * ClassAdapter used to add transactional support.
 */
public class ClassAdapterMetric extends ClassVisitor implements Opcodes {

  public static final String METRIC_MANAGER = "org/avaje/metric/MetricManager";
  public static final String METRIC_MANAGER_GET_METHOD = "getTimedMetric";
  public static final String COLLECTOR = "org/avaje/metric/TimedMetric";
  public static final String LCOLLECTOR = "L"+COLLECTOR+";";
  public static final String COLLECTOR_END_METHOD = "operationEnd";
  

  private final EnhanceContext enhanceContext;

  private boolean markerAnnotationAdded;
  
  private boolean detectJaxrs;

  private boolean detectSpringComponent;

  private boolean detectExplicit;

  private boolean shouldBeEnhanced;

  private String className;
  
  private ArrayList<String> methodsEnhanced = new ArrayList<String>();
  
  public ClassAdapterMetric(ClassVisitor cv, EnhanceContext context) {
    super(ASM4, cv);
    this.enhanceContext = context;
  }

  protected boolean isLog(int level) {
    return enhanceContext.isLog(level);
  }

  protected void log(int level, String msg) {
    if (isLog(level)) {
      enhanceContext.log(className, msg);
    }
  }
  
  protected void log(String msg) {
    enhanceContext.log(className, msg);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    super.visit(version, access, name, signature, superName, interfaces);
    if ((access & Opcodes.ACC_INTERFACE) != 0) {
      throw new NoEnhancementRequiredException("No interfaces");
    }
    this.className = name;
  }

  /**
   * Visit class level annotations.
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

    AnnotationVisitor av = super.visitAnnotation(desc, visible);

    log(8,"check annotation "+desc);
    
    if (desc.equals("Lorg/avaje/metric/agent/AlreadyEnhancedMarker;")) {
      throw new AlreadyEnhancedException("Already enhanced");
    }

    if (desc.equals("Lorg/avaje/metric/annotation/Timed;")) {
      log(5,"found Timed annotation "+desc);
      detectExplicit = true;
      shouldBeEnhanced = true;
      return av;
    }
    
    if (desc.startsWith("Ljavax/ws/rs")) {
      detectJaxrs = true;
      shouldBeEnhanced = true;
      return av;
    }

    if (desc.startsWith("Lorg/springframework/stereotype")) {
      detectSpringComponent = true;
      shouldBeEnhanced = true;
      return av;
    }
    return av;
  }

  private void addMarkerAnnotation() {
    
    if (!markerAnnotationAdded) {
      log(3,"adding marker annotation - detection explicit:"+detectExplicit+" jaxrs:"+detectJaxrs+" spring:"+detectSpringComponent);
      AnnotationVisitor av = cv.visitAnnotation("Lorg/avaje/metric/agent/AlreadyEnhancedMarker;", true);
      if (av != null) {
        av.visitEnd();
      }
      markerAnnotationAdded = true;
    }
  }
    
  /**
   * Visit the methods specifically looking for method level transactional
   * annotations.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

    if (!shouldBeEnhanced) {
      log(8, "no marker annotations found ");
      throw new NoEnhancementRequiredException();
    } else {
      addMarkerAnnotation();
    }
    
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (name.equals("<init>")) {
      // not enhancing constructor
      return mv;
    }
    if ((access & Opcodes.ACC_STATIC) != 0) {
      log(5, "not enhancing static method:"+name);
      return mv;
    }
    boolean publicMethod = isPublicMethod(access);
    if (publicMethod || isProtectedMethod(access)) {
      int metricIndex = methodsEnhanced.size();
      String metricName = addMetricName(className.replace('/', '.')+"."+name);
      if (isLog(8)) {
        log("adding timer metric to method:"+name+" metricIndex:"+metricIndex+" metricName:"+metricName);
      }
      return new AddTimerMetricMethodAdapter(enhanceContext, publicMethod, className, metricIndex, mv, access, name, desc);
    }
    
    // not enhancing non-public method
    log(3,"not enhancing non-public method:"+name);
    return mv;
  }

  private boolean isPublicMethod(int access) {
    return ((access & Opcodes.ACC_PUBLIC) != 0);
  }
  
  private boolean isProtectedMethod(int access) {
    return ((access & Opcodes.ACC_PROTECTED) != 0);
  }
  
  private String addMetricName(String baseMetricName) {
    int i = 1;
    String metricName = baseMetricName;
    while (methodsEnhanced.contains(metricName)) {
      metricName= baseMetricName+(i++);
    }
    methodsEnhanced.add(metricName);
    return metricName;
  }
  
  private String getMappedName(String rawName) {
    return enhanceContext.getMappedName(rawName);
  }

  private void addInitialisers() {
    MethodVisitor mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    
    for (int i = 0; i < methodsEnhanced.size(); i++) {
      
      String fullMethodName = methodsEnhanced.get(i);
      String fullMetricName = getMappedName(fullMethodName);
            
      log(1, "#### METRIC["+fullMetricName+"]   METHOD["+fullMethodName+"]");
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(1, l0);
      mv.visitLdcInsn(fullMetricName);
      mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, METRIC_MANAGER_GET_METHOD, "(Ljava/lang/String;)"+LCOLLECTOR);
      mv.visitFieldInsn(PUTSTATIC, className, "_$metric_"+i, LCOLLECTOR);
    }
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
  }
  
  @Override
  public void visitEnd() {
    
    for (int i = 0; i < methodsEnhanced.size(); i++) {
      FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_$metric_"+i, LCOLLECTOR, null, null);
      fv.visitEnd();      
    }
    
    addInitialisers();
    
    super.visitEnd();
  }
}
