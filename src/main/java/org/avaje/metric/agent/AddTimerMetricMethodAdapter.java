package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.FieldVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;
import org.avaje.metric.agent.asm.Type;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;

import java.util.Arrays;

/**
 * Enhances a method adding support for using TimerMetric or BucketTimerMetric to collect method
 * execution time.
 */
public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private static final String TIMED_METRIC = "org/avaje/metric/TimedMetric";

  private static final String LTIMED_METRIC = "Lorg/avaje/metric/TimedMetric;";

  private static final String METRIC_MANAGER = "org/avaje/metric/MetricManager";

  private static final String METHOD_OPERATION_END = "operationEnd";

  private static final String METHOD_IS_ACTIVE_THREAD_CONTEXT = "isActiveThreadContext";

  private final ClassAdapterMetric classAdapter;

  private final EnhanceContext context;

  private Label startFinally = new Label();

  private final String className;

  private final String methodName;

  private final int metricIndex;

  private String name;

  private String fullName;

  private String prefix;

  private int[] buckets;

  private int posUseContext;

  private int posTimeStart;

  private boolean detectNotTimed;

  private boolean enhanced;

  AddTimerMetricMethodAdapter(ClassAdapterMetric classAdapter, boolean enhanceDefault,
                              int metricIndex, String uniqueMethodName, MethodVisitor mv, int acc, String name, String desc) {

    super(ASM6, mv, acc, name, desc);
    this.classAdapter = classAdapter;
    this.context = classAdapter.getEnhanceContext();
    this.className = classAdapter.className;
    this.methodName = name;
    this.metricIndex = metricIndex;
    this.name = uniqueMethodName;
    this.enhanced = enhanceDefault;
  }

  /**
   * Return true if this method was enhanced.
   */
  public boolean isEnhanced() {
    return enhanced;
  }

  /**
   * Set by Timed annotation name attribute.
   */
  private void setName(String metricName) {
    metricName = metricName.trim();
    if (metricName.length() > 0) {
      this.name = metricName;
    }
  }

  /**
   * Set by Timed annotation fullName attribute.
   */
  private void setFullName(String fullName) {
    this.fullName = fullName;
  }

  private void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Set the bucket ranges to use for this metric/method.
   */
  private void setBuckets(Object bucket) {
    this.buckets = (int[]) bucket;
  }

  /**
   * Return the bucket ranges to be used for this metric/method.
   */
  private int[] getBuckets() {
    if (buckets != null && buckets.length > 0) {
      return buckets;
    }
    return classAdapter.getBuckets();
  }

  /**
   * Return true if there are bucket defined.
   */
  private boolean hasBuckets() {
    return buckets != null && buckets.length > 0 || classAdapter.hasBuckets();
  }

  /**
   * Get the TimeMetric or BucketTimedMetric type.
   */
  private String getMetricType() {
    return TIMED_METRIC;
  }

  /**
   * Get the TimeMetric or BucketTimedMetric type.
   */
  private String getLMetricType() {
    return LTIMED_METRIC;
  }

  /**
   * Get the unique metric name.
   */
  private String getUniqueMetricName() {
    if (hasValue(fullName)) {
      return fullName.trim();
    }
    if (hasValue(prefix)) {
      return prefix + "." + classAdapter.getShortName() + "." + name.trim();

    } else {
      return classAdapter.getMetricPrefix() + "." + name.trim();
    }
  }

  private boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
  }

  public void visitCode() {
    super.visitCode();
    if (enhanced) {
      mv.visitLabel(startFinally);
    }
  }

  private boolean isLog(int level) {
    return context.isLog(level);
  }

  private void log(int level, String msg, String extra) {
    context.log(level, msg, extra);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

    AnnotationVisitor av = super.visitAnnotation(desc, visible);
    if (detectNotTimed) {
      // just ignore
      return av;
    }

    if (isLog(7)) {
      log(7, "... check method annotation ", desc);
    }
    if (AnnotationInfo.isNotTimed(desc)) {
      // definitely do not enhance this method
      log(4, "... found NotTimed", desc);
      detectNotTimed = true;
      enhanced = false;
      return av;
    }

    if (AnnotationInfo.isTimed(desc)) {
      log(4, "... found Timed annotation ", desc);
      enhanced = true;
      return new TimedAnnotationVisitor(av);
    }

    if (AnnotationInfo.isPostConfigured(desc)) {
      log(4, "... found postConfigured annotation ", desc);
      detectNotTimed = true;
      enhanced = false;
      return av;
    }

    if (context.isIncludeJaxRS() && AnnotationInfo.isJaxrsEndpoint(desc)) {
      log(4, "... found jaxrs annotation ", desc);
      enhanced = true;
      return av;
    }

    return av;
  }

  /**
   * Helper to read and set the name and fullName attributes of the Timed annotation.
   */
  private class TimedAnnotationVisitor extends AnnotationVisitor {

    TimedAnnotationVisitor(AnnotationVisitor av) {
      super(ASM6, av);
    }

    @Override
    public void visit(String name, Object value) {
      if ("name".equals(name) && isNotEmpty(value)) {
        setName(value.toString());

      } else if ("fullName".equals(name) && isNotEmpty(value)) {
        setFullName(value.toString());

      } else if ("prefix".equals(name) && isNotEmpty(value)) {
        setPrefix(value.toString());

      } else if ("buckets".equals(name)) {
        setBuckets(value);
      }
    }

    private boolean isNotEmpty(Object value) {
      return !"".equals(value);
    }
  }


  @Override
  public void visitMaxs(int maxStack, int maxLocals) {
    if (!enhanced) {
      super.visitMaxs(maxStack, maxLocals);
    } else {
      Label endFinally = new Label();
      mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
      mv.visitLabel(endFinally);

      onFinally(ATHROW);
      mv.visitInsn(ATHROW);
      mv.visitMaxs(maxStack, maxLocals);
    }
  }

  private void onFinally(int opcode) {

    if (enhanced) {
      if (opcode == ATHROW) {
        if (isLog(8)) {
          log(8, "... add visitFrame in ", name);
        }
        mv.visitFrame(Opcodes.F_SAME, 1, new Object[]{Opcodes.LONG}, 0, null);
      }

      // load opcode
      // load startNanos
      // call interface method operationEnd(opCode, startNanos)
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(1, l5);
      mv.visitFieldInsn(GETSTATIC, className, "_$metric_" + metricIndex, getLMetricType());
      visitIntInsn(SIPUSH, opcode);
      loadLocal(posTimeStart);
      if (context.isIncludeRequestTiming()) {
        loadLocal(posUseContext);
        mv.visitMethodInsn(INVOKEINTERFACE, getMetricType(), METHOD_OPERATION_END, "(IJZ)V", true);
      } else {
        mv.visitMethodInsn(INVOKEINTERFACE, getMetricType(), METHOD_OPERATION_END, "(IJ)V", true);
      }
    }
  }

  protected void onMethodExit(int opcode) {
    if (opcode != ATHROW) {
      onFinally(opcode);
    }
  }

  @Override
  protected void onMethodEnter() {
    if (enhanced) {
      if (context.isIncludeRequestTiming()) {
        posUseContext = newLocal(Type.BOOLEAN_TYPE);
        mv.visitFieldInsn(GETSTATIC, className, "_$metric_" + metricIndex, getLMetricType());
        mv.visitMethodInsn(INVOKEINTERFACE, getMetricType(), METHOD_IS_ACTIVE_THREAD_CONTEXT, "()Z", true);
        mv.visitVarInsn(ISTORE, posUseContext);
      }
      posTimeStart = newLocal(Type.LONG_TYPE);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
      mv.visitVarInsn(LSTORE, posTimeStart);
    }
  }


  void addFieldInitialisation(MethodVisitor mv, int i) {

    if (!isEnhanced()) {
      log(2, "--- not enhanced (maybe protected/private) ", methodName);

    } else {
      // apply any metric name mappings to the uniqueMethodName to get
      // the final metric name that will be used
      String mappedMetricName = getUniqueMetricName();
      context.logAddingMetric(mappedMetricName);
      if (isLog(1)) {
        log(1, "# Add Metric[" + mappedMetricName + "] index[" + i + "]", "");
      }

      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(1, l0);
      mv.visitLdcInsn(mappedMetricName);

      int[] buckets = getBuckets();
      if (buckets == null || buckets.length == 0) {
        // A TimedMetric
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, "getTimedMetric", "(Ljava/lang/String;)Lorg/avaje/metric/TimedMetric;", false);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_" + i, LTIMED_METRIC);

      } else {
        // A BucketTimedMetric so need to create with the bucket array
        if (isLog(3)) {
          log(3, "... init with buckets", Arrays.toString(buckets));
        }

        push(mv, buckets.length);
        mv.visitIntInsn(NEWARRAY, T_INT);
        for (int j = 0; j < buckets.length; j++) {
          mv.visitInsn(DUP);
          push(mv, j);
          push(mv, buckets[j]);
          mv.visitInsn(IASTORE);
        }
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, "getTimedMetric", "(Ljava/lang/String;[I)Lorg/avaje/metric/TimedMetric;", false);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_" + i, LTIMED_METRIC);
      }
    }
  }

  void addFieldDefinition(ClassVisitor cv, int i) {
    if (isEnhanced()) {
      if (isLog(4)) {
        log(4, "... init field index[" + i + "] METHOD[" + getUniqueMetricName() + "]", "");
      }
      FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_$metric_" + i, getLMetricType(), null, null);
      fv.visitEnd();
    }
  }

  /**
   * Helper method to visit a put integer that takes into account the value and size.
   */
  private void push(MethodVisitor mv, final int value) {
    if (value >= -1 && value <= 5) {
      mv.visitInsn(Opcodes.ICONST_0 + value);
    } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.BIPUSH, value);
    } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.SIPUSH, value);
    } else {
      mv.visitLdcInsn(value);
    }
  }

}
