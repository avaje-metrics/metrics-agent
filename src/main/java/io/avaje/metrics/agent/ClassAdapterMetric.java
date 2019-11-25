package io.avaje.metrics.agent;

import io.avaje.metrics.agent.asm.AnnotationVisitor;
import io.avaje.metrics.agent.asm.ClassVisitor;
import io.avaje.metrics.agent.asm.Label;
import io.avaje.metrics.agent.asm.MethodVisitor;
import io.avaje.metrics.agent.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassAdapter used to add metrics collection.
 */
public class ClassAdapterMetric extends ClassVisitor implements Opcodes {

  private static final String SINGLETON = "/Singleton;";

  private static final String SPRINGFRAMEWORK_STEREOTYPE = "Lorg/springframework/stereotype";

  private static final String ANNOTATION_TIMED = "Lio/avaje/metrics/annotation/Timed;";

  private static final String ANNOTATION_NOT_TIMED = "Lio/avaje/metrics/annotation/NotTimed;";

  private static final String ANNOTATION_ALREADY_ENHANCED_MARKER = "Lio/avaje/metrics/spi/AlreadyEnhancedMarker;";

  private final EnhanceContext enhanceContext;

  private boolean markerAnnotationAdded;

  private boolean detectSingleton;

  private boolean detectWebController;

  private boolean detectJaxrs;

  private boolean detectSpringComponent;

  private boolean detectExplicit;

  private boolean enhanceClassLevel;

  private boolean existingStaticInitialiser;

  protected String className;

  private String longName;
  private String shortName;
  private boolean explicitFullName;

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

  private String prefix;

  /**
   * The buckets defined commonly for all enhanced methods for this class.
   */
  private int[] buckets;

  /**
   * Construct with visitor, context and classLoader.
   */
  ClassAdapterMetric(ClassVisitor cv, EnhanceContext context) {
    super(ASM7, cv);
    this.enhanceContext = context;
  }

  EnhanceContext getEnhanceContext() {
    return enhanceContext;
  }

  boolean isLog(int level) {
    return enhanceContext.isLog(level);
  }

  private void log(int level, String msg) {
    if (isLog(level)) {
      enhanceContext.log(className, msg);
    }
  }

  private void log(int level, String msg, String extra, String extra2, String extra3) {
    if (isLog(level)) {
      enhanceContext.log(className, msg + extra + extra2 + extra3);
    }
  }

  private void log(int level, String msg, String extra) {
    if (isLog(level)) {
      enhanceContext.log(className, msg + extra);
    }
  }

  void log(String msg) {
    enhanceContext.log(className, msg);
  }

  /**
   * Set default buckets to use for methods enhanced for this class.
   */
  private void setBuckets(Object value) {
    this.buckets = (int[]) value;
  }

  /**
   * Return true if there are default buckets defined at the class level.
   */
  boolean hasBuckets() {
    return buckets != null && buckets.length > 0;
  }

  /**
   * Return the bucket ranges.
   */
  int[] getBuckets() {
    return buckets;
  }

  /**
   * Return the class level metric prefix used to prefix timed metrics on methods.
   */
  String getMetricPrefix() {
    if (prefix != null) {
      return prefix + "." + shortName;
    }
    if ((detectWebController) && !explicitFullName) {
      return deriveControllerName();
    }
    return enhanceContext.isNameIncludesPackage() ? longName : shortName;
//    if (enhanceContext.isNameIncludesPackage()) {
//      return longName;
//    }
//    return explicitFullName ? shortName : "app." + shortName;
  }

  private String deriveControllerName() {
    return "web.api." + shortName;//TrimController.trim(shortName);
  }

  String getShortName() {
    return shortName;
  }

  /**
   * Set the metric name via Timer annotation.
   */
  private void setShortName(String shortName) {
    this.shortName = shortName;
  }

  private void setLongName(String fullName) {
    this.explicitFullName  = true;
    this.prefix = null;
    this.longName = fullName;
    this.shortName = fullName;
  }

  private void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  private void setClassName(String className) {
    this.className = className;
    this.longName = className.replace('/', '.');
    int lastDot = longName.lastIndexOf('.');
    if (lastDot > -1) {
      shortName = longName.substring(lastDot + 1);
    } else {
      shortName = longName;
    }
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    super.visit(version, access, name, signature, superName, interfaces);
    if ((access & Opcodes.ACC_INTERFACE) != 0) {
      throw new NoEnhancementRequiredException("Not enhancing interface");
    }

    setClassName(name);
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
      enhanceClassLevel = true;
      // read the name and bucket ranges from the class level Timer annotation
      return new ClassTimedAnnotationVisitor(av);
    }

    if (isWebEndpoint(desc)) {
      log(5, "found web endpoint annotation ", desc);
      detectWebController = true;
      enhanceClassLevel = true;
    }

    if (enhanceContext.isEnhanceSingleton() && desc.endsWith(SINGLETON)) {
      detectSingleton = true;
      enhanceClassLevel = true;
    }

    if (enhanceContext.isIncludeJaxRS() && isJaxRsEndpoint(desc)) {
      detectJaxrs = true;
      enhanceClassLevel = true;
    }

    // We are interested in Service, Controller, Component etc
    if (enhanceContext.isIncludeSpring() && desc.startsWith(SPRINGFRAMEWORK_STEREOTYPE)) {
      detectSpringComponent = true;
      enhanceClassLevel = true;
    }
    return av;
  }

  /**
   * Return true if this annotation marks a Rest Controller.
   */
  private boolean isWebEndpoint(String desc) {
    return desc.equals("Lio/dinject/controller/Path;")
      || desc.equals("Lio/dinject/controller/Controller;");
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
        String flagWeb = (detectWebController ? "WebApi " : "");
        String flagJaxrs = (detectJaxrs ? "JAXRS " : "");
        String flagSpring = (detectSpringComponent ? "SPRING " : "");
        String flagSingleton = (detectSingleton ? "SINGLETON" : "");
        log(4, "enhancing - detection ", flagExplicit + flagWeb + flagJaxrs + flagSpring + flagSingleton);
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
  private class ClassTimedAnnotationVisitor extends AnnotationVisitor {

    ClassTimedAnnotationVisitor(AnnotationVisitor av) {
      super(ASM7, av);
    }

    @Override
    public void visit(String name, Object value) {
      if ("name".equals(name) && !"".equals(value)) {
        setShortName(value.toString());

      } else if ("fullName".equals(name) && !"".equals(value)) {
        setLongName(value.toString());

      } else if ("buckets".equals(name)) {
        setBuckets(value);

      } else if ("prefix".equals(name)) {
        setPrefix(value.toString());
      }
    }
  }

  /**
   * Visit the methods specifically looking for method level transactional annotations.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

    addMarkerAnnotation();

    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (name.equals("<init>")) {
      // not enhancing constructor
      log(5, "... not enhancing constructor:", name, " desc:", desc);
      return mv;
    }

    if (isCommonMethod(name)) {
      // not enhancing constructor
      log(5, "... not enhancing:", name, " desc:", desc);
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

    boolean enhanceByDefault = enhanceClassLevel && publicMethod;
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

    if (noTimedMethods()) {
      log(8, "... no timed methods, not enhancing");
      throw new NoEnhancementRequiredException();
    }

    addStaticFieldDefinitions();
    addStaticFieldInitialisers();

    if (!existingStaticInitialiser) {
      log(5, "... add <clinit> to call _$initMetrics()");
      addStaticInitialiser();
    }

    super.visitEnd();
  }

  /**
   * Return true if all the methods have no enhancement required.
   */
  private boolean noTimedMethods() {
    for (AddTimerMetricMethodAdapter methodAdapter : methodAdapters) {
      if (methodAdapter.isEnhanced()) {
        return false;
      }
    }
    return true;
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
