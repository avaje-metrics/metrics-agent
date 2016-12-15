package org.avaje.metric.agent;

import java.util.ArrayList;
import java.util.List;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;

/**
 * ClassAdapter used to add metrics collection.
 */
public class ClassAdapterMetric extends ClassVisitor implements Opcodes {

  private static final String SINGLETON = "/Singleton;";

  private static final String SPRINGFRAMEWORK_STEREOTYPE = "Lorg/springframework/stereotype";

  private static final String ANNOTATION_TIMED = "Lorg/avaje/metric/annotation/Timed;";

  private static final String ANNOTATION_NOT_TIMED = "Lorg/avaje/metric/annotation/NotTimed;";

  private static final String ANNOTATION_ALREADY_ENHANCED_MARKER = "Lorg/avaje/metric/spi/AlreadyEnhancedMarker;";

  protected final EnhanceContext enhanceContext;

  protected final ClassLoader classLoader;

  private boolean markerAnnotationAdded;

  private boolean detectSingleton;
  
  private boolean detectJaxrs;

  private boolean detectSpringComponent;

  private boolean detectExplicit;

  private boolean shouldBeEnhanced;

  private boolean existingStaticInitialiser;

  protected String className;

  /**
   * List of unique names to support parameter overloading.
   */
  private final ArrayList<String> uniqueMethodNames = new ArrayList<>();

  /**
   * The method adapters that detect if a method is enhanced and perform the enhancement.
   */
  private final List<AddTimerMetricMethodAdapter> methodAdapters = new ArrayList<>();

  /**
   * The metric full name that is a common prefix for each method.
   */
  private String metricFullName;
  private String originalMetricName;

  /**
   * The buckets defined commonly for all enhanced methods for this class.
   */
  private int[] buckets;
  
  /**
   * Construct with visitor, context and classLoader.
   */
  public ClassAdapterMetric(ClassVisitor cv, EnhanceContext context, ClassLoader classLoader) {
    super(ASM5, cv);
    this.enhanceContext = context;
    this.classLoader = classLoader;
  }

  protected boolean isLog(int level) {
    return enhanceContext.isLog(level);
  }
  
  protected void log(int level, String msg) {
    if (isLog(level)) {
      enhanceContext.log(className, msg);
    }
  }

  protected void log(int level, String msg, String extra, String extra2, String extra3) {
    if (isLog(level)) {
      enhanceContext.log(className, msg + extra + extra2 + extra3);
    }
  }

  protected void log(int level, String msg, String extra) {
    if (isLog(level)) {
      enhanceContext.log(className, msg + extra);
    }
  }

  protected void log(String msg) {
    enhanceContext.log(className, msg);
  }

  /**
   * Set default buckets to use for methods enhanced for this class.
   */
  protected void setBuckets(Object value) {
    this.buckets = (int[])value;
  }
  
  /**
   * Return true if there are default buckets defined at the class level.
   */
  protected boolean hasBuckets() {
    return buckets != null && buckets.length > 0;
  }
  
  /**
   * Return the bucket ranges.
   */
  protected int[] getBuckets() {
    return buckets;
  }
  
  /**
   * Return the class level metric name which is used to prefix the metrics created for associated
   * methods on this class.
   */
  protected String getMetricFullName() {
    return metricFullName;
  }

  /**
   * Set the metric name via Timer annotation.
   */
  protected void setMetricName(String metricName) {
    int pos = metricFullName.lastIndexOf('.');
    if (pos == -1) {
      this.metricFullName = metricName;
    } else {
      this.metricFullName = metricFullName.substring(0, pos) + "." + metricName;
    }
  }
  
  /**
   * Set the full metric name for this class.
   */
  private void setMetricFullName(String className) {
    this.metricFullName = className.replace('/', '.');
    this.originalMetricName = metricFullName;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    super.visit(version, access, name, signature, superName, interfaces);
    if ((access & Opcodes.ACC_INTERFACE) != 0) {
      throw new NoEnhancementRequiredException("Not enhancing interface");
    }

    this.className = name;
    setMetricFullName(className);

    NameMapping.Match match = enhanceContext.findMatch(metricFullName);
    if (match != null) {
      if (!match.include) {
        throw new NoEnhancementRequiredException("Excluded by match "+match.pattern);
      } else {
        detectExplicit = true;
        shouldBeEnhanced = true;
        buckets = match.buckets;
      }
    }
  }

  /**
   * Visit class level annotations.
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

    AnnotationVisitor av = super.visitAnnotation(desc, visible);

    log(8, "... check annotation ", desc);

    if (desc.equals(ANNOTATION_ALREADY_ENHANCED_MARKER)) {
      throw new AlreadyEnhancedException("Already enhanced");
    }

    if (desc.equals(ANNOTATION_NOT_TIMED)) {
      throw new NoEnhancementRequiredException("marked as NotTimed");
    }

    if (desc.equals(ANNOTATION_TIMED)) {
      log(5, "found Timed annotation ", desc);
      detectExplicit = true;
      shouldBeEnhanced = true;
      // read the name and bucket ranges from the class level Timer annotation
      return new TimedAnnotationVisitor(av);
    }
    
    if (enhanceContext.isEnhanceSingleton() && desc.endsWith(SINGLETON)) {
      detectSingleton = true;
      shouldBeEnhanced = true;      
    }

    if (isJaxRsEndpoint(desc)) {
      detectJaxrs = true;
      shouldBeEnhanced = true;
    }

    // We are interested in Service, Controller, Component etc
    if (desc.startsWith(SPRINGFRAMEWORK_STEREOTYPE)) {
      detectSpringComponent = true;
      shouldBeEnhanced = true;
    }
    return av;
  }

  private boolean isJaxRsEndpoint(String desc) {

    return desc.equals("Ljavax/ws/rs/Path;") 
        || desc.equals("Ljavax/ws/rs/Produces;")
        || desc.equals("Ljavax/ws/rs/Consumes;");
  }

  private void addMarkerAnnotation() {

    if (!markerAnnotationAdded) {
      if (isLog(4)) {
        String flagExplicit = (detectExplicit ? "EXPLICIT " : "");
        String flagJaxrs = (detectJaxrs ? "JAXRS " : "");
        String flagSpring = (detectSpringComponent ? "SPRING " : "");
        String flagSingleton = (detectSingleton ? "SINGLETON" : "");
        log(4, "enhancing - detection ", flagExplicit + flagJaxrs + flagSpring + flagSingleton );
      }
      AnnotationVisitor av = cv.visitAnnotation(ANNOTATION_ALREADY_ENHANCED_MARKER, true);
      if (av != null) {
        av.visitEnd();
      }
      markerAnnotationAdded = true;
    }
  }

  /**
   * Helper to read and set the name and fullName attributes of the Timed annotation.
   */
  private class TimedAnnotationVisitor extends AnnotationVisitor {

    public TimedAnnotationVisitor(AnnotationVisitor av) {
      super(ASM4, av);
    }

    @Override
    public void visit(String name, Object value) {
      if ("name".equals(name) && !"".equals(value)) {
        setMetricName(value.toString());
        
      } else if ("fullName".equals(name) && !"".equals(value)) {
        setMetricFullName(value.toString());
        
      } else if ("buckets".equals(name)) {
        setBuckets(value);
      }
    }
  }
  
  /**
   * Visit the methods specifically looking for method level transactional annotations.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

    if (!shouldBeEnhanced) {
      log(8, "... no marker annotations found ");
      throw new NoEnhancementRequiredException();
    } else {
      addMarkerAnnotation();
    }

    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (name.equals("<init>")) {
      // not enhancing constructor
      log(5, "... not enhancing constructor:", name, " desc:", desc);
      return mv;
    }
    if (enhanceContext.isMatchExcludeMethod(originalMetricName, name)) {
      log(5, "... exclude method:", name, " desc:", desc);
      return mv;
    }
    if (isCommonMethod(name)) {
      // not enhancing constructor
      log(5, "... not enhancing:",  name, " desc:", desc);
      return mv;
    }
    if (name.equals("<clinit>")) {
      // static initializer, add call _$initMetrics()
      log(5, "... <clinit> exists - adding call to _$initMetrics()", "");
      existingStaticInitialiser = true;
      return new StaticInitAdapter(mv, access, name, desc, className);
    }
    
    boolean publicMethod = isPublicMethod(access);
    int metricIndex = methodAdapters.size();
    String uniqueMethodName = deriveUniqueMethodName(name);
    if (isLog(8)) {
      log("... method:" + name + " public:" + publicMethod + " index:" + metricIndex + " uniqueMethodName:" + uniqueMethodName);
    }

    boolean enhanceByDefault = publicMethod;
    if ((access & Opcodes.ACC_STATIC) != 0) {
      // by default not enhancing static method unless it is explicitly
      // annotated with a Timed annotation
      enhanceByDefault = enhanceContext.isIncludeStaticMethods();
      if (isLog(5)) {
        log(5, "... static method:", name, " desc:", desc + " - enhanceByDefault" + enhanceByDefault);
      }
    }
    if (isPostConfiguredMethod(name)) {
      if (isLog(8)) {
        log("... method:" + name + " not enhanced by default (as postConfigured or init method)");
      }
      enhanceByDefault = false;
    }
    // Not sure if we are enhancing this method yet ...
    AddTimerMetricMethodAdapter methodAdapter = createAdapter(enhanceByDefault, metricIndex, uniqueMethodName, mv, access, name, desc);
    methodAdapters.add(methodAdapter);
    return methodAdapter;
  }

  /**
   * Return true if a equals, hashCode or toString method - these are not enhanced.
   */
  private boolean isCommonMethod(String name) {

    return name.equals("equals") || name.equals("hashCode") || name.equals("toString");
  }

  /**
   * By default ignore these postConfigured/init type methods.
   */
  private boolean isPostConfiguredMethod(String name) {
    return name.equals("init") || name.equals("postConfigured");
  }

  private AddTimerMetricMethodAdapter createAdapter(boolean enhanceDefault, int metricIndex, String uniqueMethodName, MethodVisitor mv, int access, String name, String desc) {

    return new AddTimerMetricMethodAdapter(this, enhanceDefault, metricIndex, uniqueMethodName, mv, access, name, desc);
  }

  private boolean isPublicMethod(int access) {
    return ((access & Opcodes.ACC_PUBLIC) != 0);
  }

  /**
   * Create and return a unique method name in case of parameter overloading.
   */
  private String deriveUniqueMethodName(String methodName) {
    
    int i = 1;
    String uniqueMethodName = methodName;
    while (uniqueMethodNames.contains(uniqueMethodName)) {
      uniqueMethodName = methodName + (i++);
    }
    uniqueMethodNames.add(uniqueMethodName);
    return uniqueMethodName;
  }


  @Override
  public void visitEnd() {

    addStaticFieldDefinitions();
    addStaticFieldInitialisers();

    if (!existingStaticInitialiser) {
      log(5, "... add <clinit> to call _$initMetrics()");
      addStaticInitialiser();
    }

    super.visitEnd();
  }

  private void addStaticFieldDefinitions() {
    for (int i = 0; i < methodAdapters.size(); i++) {
      AddTimerMetricMethodAdapter methodAdapter = methodAdapters.get(i);
      methodAdapter.addFieldDefinition(cv, i);
    }
  }
  

  /**
   * Add the static _$initMetrics() method.
   */
  private void addStaticFieldInitialisers() {

    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "_$initMetrics", "()V", null, null);
    mv.visitCode();

    log(4, "... adding static _$initMetrics() method");

    for (int i = 0; i < methodAdapters.size(); i++) {

      AddTimerMetricMethodAdapter methodAdapter = methodAdapters.get(i);
      methodAdapter.addFieldInitialisation(mv, i);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
  }

  /**
   * Add a static initialization block when there was not one on the class.
   */
  private void addStaticInitialiser() {
    
    MethodVisitor mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(16, l0);
    mv.visitMethodInsn(INVOKESTATIC, className, "_$initMetrics", "()V", false);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLineNumber(17, l1);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

}
