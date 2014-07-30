package org.avaje.metric.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.FieldVisitor;
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

  private static final String ANNOTATION_ALREADY_ENHANCED_MARKER = "Lorg/avaje/metric/agent/AlreadyEnhancedMarker;";

  public static final String METRIC_MANAGER = "org/avaje/metric/MetricManager";

  public static final String METRIC_MANAGER_GET_METHOD = "getTimedMetric";

  public static final String COLLECTOR = "org/avaje/metric/TimedMetric";

  public static final String LCOLLECTOR = "L" + COLLECTOR + ";";

  public static final String COLLECTOR_END_METHOD = "operationEnd";

  private final EnhanceContext enhanceContext;

  private final ClassLoader classLoader;

  private boolean markerAnnotationAdded;

  private boolean detectSingleton;
  
  private boolean detectJaxrs;

  private boolean detectSpringComponent;

  private boolean detectExplicit;

  private boolean shouldBeEnhanced;

  private boolean existingStaticInitialiser;

  private String className;

  private ClassMeta superMeta;

  private Set<String> enhancedMethods;

  private List<MethodMeta> extraProxyMethods;

  /**
   * List of unique names to support parameter overloading.
   */
  private final ArrayList<String> uniqueMethodNames = new ArrayList<String>();

  /**
   * The method adapters that detect if a method is enhanced and perform the enhancement.
   */
  private final List<AddTimerMetricMethodAdapter> methodAdapters = new ArrayList<AddTimerMetricMethodAdapter>();

  /**
   * Construct with visitor, context and classLoader.
   */
  public ClassAdapterMetric(ClassVisitor cv, EnhanceContext context, ClassLoader classLoader) {
    super(ASM4, cv);
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

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    super.visit(version, access, name, signature, superName, interfaces);
    if ((access & Opcodes.ACC_INTERFACE) != 0) {
      throw new NoEnhancementRequiredException("Not enhancing interface");
    }

    this.className = name;

    if (!superName.equals("java/lang/Object")) {
      // read information about superClasses...
      if (isLog(7)) {
        log("read information about superClasses " + superName + " to find methods that need ");
      }
      ClassMeta superMeta = enhanceContext.getSuperMeta(superName, classLoader);
      if (superMeta != null && superMeta.isCheckForMethodsToProxy()) {
        // the superClass is abstract so need to look at it to see if it has
        // any public methods etc that we should proxy so that the statistics
        // better reflect what is happening...
        this.superMeta = superMeta;
        if (isLog(1)) {
          log("entity extends " + superMeta.getDescription());
        }
      } else {
        if (isLog(7)) {
          if (superMeta == null) {
            log("unable to read superMeta for " + superName);
          } else {
            log("superMeta " + superName + " is not an entity/embedded/mappedsuperclass "
                + superMeta.getClassAnnotations());
          }
        }
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
      return av;
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
      if (isLog(3)) {
        String flagExplicit = (detectExplicit ? "EXPLICIT " : "");
        String flagJaxrs = (detectJaxrs ? "JAXRS " : "");
        String flagSpring = (detectSpringComponent ? "SPRING " : "");
        String flagSingleton = (detectSingleton ? "SINGLETON" : "");
        log(3, "enhancing - detection ", flagExplicit + flagJaxrs + flagSpring + flagSingleton );
      }
      AnnotationVisitor av = cv.visitAnnotation(ANNOTATION_ALREADY_ENHANCED_MARKER, true);
      if (av != null) {
        av.visitEnd();
      }
      markerAnnotationAdded = true;
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
    if (isCommonMethod(name, desc)) {
      // not enhancing constructor
      log(5, "... not enhancing:",  name, " desc:", desc);
      return mv;
    }
    if (name.equals("<clinit>")) {
      // static initialiser, add call _$initMetrics()
      log(2, "... <clinit> exists - adding call to _$initMetrics()", "");
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
      enhanceByDefault = false;
      if (isLog(5)) {
        log(5, "... static method:",  name,  " desc:",  desc);
      }
    }
    
    // Not sure if we are enhancing this method yet ...
    AddTimerMetricMethodAdapter methodAdapter = createAdapter(enhanceByDefault, metricIndex, uniqueMethodName, mv, access, name, desc);
    methodAdapters.add(methodAdapter);
    return methodAdapter;
  }

  /**
   * Return true if a equals, hashCode or toString method - these are not enhanced.
   */
  private boolean isCommonMethod(String name, String desc) {
    
    if (name.equals("equals")) {
      return true;
    }
    if (name.equals("hashCode")) {
      return true;
    }
    if (name.equals("toString")) {
      return true;
    }
    return false;
  }

  private AddTimerMetricMethodAdapter createAdapter(boolean enhanceDefault, int metricIndex, String uniqueMethodName, MethodVisitor mv, int access, String name, String desc) {

    return new AddTimerMetricMethodAdapter(enhanceContext, enhanceDefault, className, metricIndex, uniqueMethodName, mv, access, name, desc);
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
    return className.replace('/', '.') + "." + uniqueMethodName;
  }

  /**
   * Return the potentially cut down metric name.
   */
  private String getMappedName(String rawName) {
    return enhanceContext.getMappedName(rawName);
  }

  /**
   * Add the static _$initMetrics() method.
   */
  private void addStaticFieldInitialisers() {

    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "_$initMetrics", "()V", null, null);
    mv.visitCode();

    log(3, "... adding static _$initMetrics() method");

    for (int i = 0; i < methodAdapters.size(); i++) {

      AddTimerMetricMethodAdapter methodAdapter = methodAdapters.get(i);
      String uniqueMethodName = methodAdapter.getUniqueMethodName();

      if (!methodAdapter.isEnhanced()) {
        log(2, "--- not enhanced ", uniqueMethodName);

      } else {
        // apply any metric name mappings to the uniqueMethodName to get
        // the final metric name that will be used
        String mappedMetricName = getMappedName(uniqueMethodName);
        if (isLog(1)) {
          if (mappedMetricName.equals(uniqueMethodName)) {
            log(1, "# Add Metric[" + mappedMetricName + "] index[" + i + "]");

          } else {
            log(1, "# Add Metric[" + mappedMetricName + "] Method[" + uniqueMethodName + "] index[" + i + "]");            
          }
        }
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(1, l0);
        mv.visitLdcInsn(mappedMetricName);
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, METRIC_MANAGER_GET_METHOD, "(Ljava/lang/String;)" + LCOLLECTOR);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_" + i, LCOLLECTOR);
      }
    }

    if (extraProxyMethods != null) {
      for (int i = 0; i < extraProxyMethods.size(); i++) {
        MethodMeta methodMeta = extraProxyMethods.get(i);
        String uniqueMethodName = methodMeta.getUniqueMethodName();
        String mappedMetricName = getMappedName(uniqueMethodName);

        if (isLog(1)) {
          log(1, "### PROXY - METRIC[" + mappedMetricName + "] METHOD[" + uniqueMethodName + "] index[" + i + "]");
        }
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(1, l0);
        mv.visitLdcInsn(mappedMetricName);
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, METRIC_MANAGER_GET_METHOD, "(Ljava/lang/String;)" + LCOLLECTOR);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metricP_" + i, LCOLLECTOR);
      }
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
  }

  @Override
  public void visitEnd() {

    determineProxyMethodsFromSuperclasses();

    addFieldDefinitions();

    addStaticFieldInitialisers();

    if (!existingStaticInitialiser) {
      log(3, "... add <clinit> to call _$initMetrics()");
      addStaticInitialiser();
    }

    super.visitEnd();
  }

  private void addFieldDefinitions() {
    for (int i = 0; i < methodAdapters.size(); i++) {
      AddTimerMetricMethodAdapter methodAdapter = methodAdapters.get(i);
      if (methodAdapter.isEnhanced()) {
        if (isLog(4)) {
          log(4, "... init field index[" + i + "] METHOD[" + methodAdapter.getUniqueMethodName() + "]");
        }
        FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_$metric_" + i, LCOLLECTOR, null, null);
        fv.visitEnd();
      }
    }

    if (extraProxyMethods != null) {
      for (int i = 0; i < extraProxyMethods.size(); i++) {
        MethodMeta methodMeta = extraProxyMethods.get(i);
        if (isLog(4)) {
          log(4, "... init proxy field index[" + i + "] METHOD[" + methodMeta.getUniqueMethodName() + "]");
        }
        FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_$metricP_" + i, LCOLLECTOR, null, null);
        fv.visitEnd();
      }
    }
  }

  private void determineProxyMethodsFromSuperclasses() {

    if (superMeta != null) {
      enhancedMethods = new HashSet<String>();
      extraProxyMethods = new ArrayList<MethodMeta>();
      for (int i = 0; i < methodAdapters.size(); i++) {
        enhancedMethods.add(methodAdapters.get(i).getNameDescription());
      }

      List<MethodMeta> allMethodMeta = superMeta.getAllMethodMeta();
      log(5, "... check inherited methods ", allMethodMeta.toString());
      for (MethodMeta methodMeta : allMethodMeta) {
        String methodNameDesc = methodMeta.getNameDescription();
        if (enhancedMethods.add(methodNameDesc)) {
          // going to add a proxy method - get a unique method name for the metric
          String uniqueMethodName = deriveUniqueMethodName(methodMeta.getName());
          methodMeta.setUniqueMethodName(uniqueMethodName);
          // add it to our list of proxy methods to generate
          extraProxyMethods.add(methodMeta);
        }
      }
      String superClass = superMeta.getClassName();
      if (isLog(4)) {
        log(4, "... superclass", superClass);
      }

      for (int i = 0; i < extraProxyMethods.size(); i++) {
        MethodMeta methodMeta = extraProxyMethods.get(i);
        if (isLog(4)) {
          log(4, "... SKIP add proxy method for index[" + i + "] METHOD[" + methodMeta.getUniqueMethodName() + "]");
        }
        // AddProxyMethodVisitor proxyAdd = new AddProxyMethodVisitor(methodMeta, className,
        // superClass);
        // proxyAdd.start(cv, i);
      }

    }
  }

  /**
   * Add a static initialisation block when there was not one on the class.
   */
  private void addStaticInitialiser() {
    MethodVisitor mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(16, l0);
    mv.visitMethodInsn(INVOKESTATIC, className, "_$initMetrics", "()V");
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLineNumber(17, l1);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

}
