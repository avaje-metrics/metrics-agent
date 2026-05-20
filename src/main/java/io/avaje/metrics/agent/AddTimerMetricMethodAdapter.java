package io.avaje.metrics.agent;

import io.avaje.metrics.agent.asm.AnnotationVisitor;
import io.avaje.metrics.agent.asm.ClassVisitor;
import io.avaje.metrics.agent.asm.FieldVisitor;
import io.avaje.metrics.agent.asm.Label;
import io.avaje.metrics.agent.asm.MethodVisitor;
import io.avaje.metrics.agent.asm.Opcodes;
import io.avaje.metrics.agent.asm.commons.AdviceAdapter;

import java.util.Arrays;

import static io.avaje.metrics.agent.Transformer.ASM_VERSION;
import static io.avaje.metrics.agent.asm.Type.LONG_TYPE;
import static io.avaje.metrics.agent.asm.Type.getObjectType;

/**
 * Enhances a method adding support for using TimerMetric or BucketTimerMetric to collect method
 * execution time.
 */
public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private static final String TIMED_METRIC = "io/avaje/metrics/Timer";
  private static final String TIMED_EVENT = "io/avaje/metrics/Timer$Event";
  private static final String THROWABLE = "java/lang/Throwable";

  private static final String LTIMED_METRIC = "Lio/avaje/metrics/Timer;";
  private static final String LTIMED_EVENT = "Lio/avaje/metrics/Timer$Event;";

  private static final String METRIC_MANAGER = "io/avaje/metrics/Metrics";

  private static final String CREATE_TIMER = "timer";
  private static final String CREATE_TRACED_TIMER = "tracedTimer";
  private static final String OPERATION_START_EVENT = "startEvent";
  private static final String OPERATION_END = "add";
  private static final String OPERATION_ERR = "addErr";
  private static final String OPERATION_EVENT_END = "end";
  private static final String OPERATION_EVENT_END_ERROR = "endWithError";
  private static final String OPERATION_EVENT_END_ERROR_DESC = "(Ljava/lang/Throwable;)V";

  private final ClassAdapterMetric classAdapter;

  private final EnhanceContext context;

  private Label startFinally = new Label();

  private final String className;

  private final String methodName;

  private final int metricIndex;

  private String name;
  private boolean explicitFullName;

  private int[] buckets;

  private int posTimeStart;
  private int posEvent;
  private int posThrowable;

  private boolean detectNotTimed;

  private boolean enhanced;
  private TimedSpanMode spanMode = TimedSpanMode.DEFAULT;

  AddTimerMetricMethodAdapter(ClassAdapterMetric classAdapter, boolean enhanceDefault,
                              int metricIndex, String uniqueMethodName, MethodVisitor mv, int acc, String name, String desc) {

    super(ASM_VERSION, mv, acc, name, desc);
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
  boolean isEnhanced() {
    return enhanced;
  }

  /**
   * Set by Timed annotation name attribute.
   */
  private void setName(String metricName) {
    metricName = metricName.trim();
    if (!metricName.isEmpty()) {
      this.name = metricName;
      this.explicitFullName = name.contains(".");
    }
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
   * Get the unique metric name.
   */
  private String getUniqueMetricName() {
    if (explicitFullName) {
      return name;
    }
    return classAdapter.getMetricPrefix() + "." + name;
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

  private boolean isTraced() {
    return context.isTimedSpansEnabled(classAdapter.getSpanMode(), spanMode);
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
    if (AnnotationInfo.isAvajeControllerMethod(desc)) {
      log(4, "... found avaje-http controller annotation ", desc);
      enhanced = true;
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
      super(ASM7, av);
    }

    @Override
    public void visit(String name, Object value) {
      if ("name".equals(name) && isNotEmpty(value)) {
        setName(value.toString());
      } else if ("buckets".equals(name)) {
        setBuckets(value);
      }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
      if ("span".equals(name)) {
        spanMode = TimedSpanMode.of(value);
      }
      super.visitEnum(name, descriptor, value);
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
      boolean isError = opcode == ATHROW;
      if (isError) {
        if (isLog(8)) {
          log(8, "... add visitFrame in ", name);
        }
        if (isTraced()) {
          mv.visitFrame(Opcodes.F_SAME, 1, new Object[]{TIMED_EVENT}, 0, null);
        } else {
          mv.visitFrame(Opcodes.F_SAME, 1, new Object[]{Opcodes.LONG}, 0, null);
        }
      }

      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(1, l5);
      if (isTraced()) {
        if (isError) {
          storeLocal(posThrowable);
        }
        loadLocal(posEvent);
        if (isError) {
          loadLocal(posThrowable);
          mv.visitMethodInsn(INVOKEINTERFACE, TIMED_EVENT, OPERATION_EVENT_END_ERROR, OPERATION_EVENT_END_ERROR_DESC, true);
          loadLocal(posThrowable);
        } else {
          mv.visitMethodInsn(INVOKEINTERFACE, TIMED_EVENT, OPERATION_EVENT_END, "()V", true);
        }
      } else {
        mv.visitFieldInsn(GETSTATIC, className, "_$metric_" + metricIndex, LTIMED_METRIC);
        loadLocal(posTimeStart);
        String methodDesc = isError ? OPERATION_ERR : OPERATION_END;
        mv.visitMethodInsn(INVOKEINTERFACE, TIMED_METRIC, methodDesc, "(J)V", true);
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
      if (isTraced()) {
        posEvent = newLocal(getObjectType(TIMED_EVENT));
        posThrowable = newLocal(getObjectType(THROWABLE));
        mv.visitFieldInsn(GETSTATIC, className, "_$metric_" + metricIndex, LTIMED_METRIC);
        mv.visitMethodInsn(INVOKEINTERFACE, TIMED_METRIC, OPERATION_START_EVENT, "()" + LTIMED_EVENT, true);
        storeLocal(posEvent);
      } else {
        posTimeStart = newLocal(LONG_TYPE);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        mv.visitVarInsn(LSTORE, posTimeStart);
      }
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
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, isTraced() ? CREATE_TRACED_TIMER : CREATE_TIMER, "(Ljava/lang/String;)Lio/avaje/metrics/Timer;", false);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_" + i, LTIMED_METRIC);

      } else {
        // A BucketTimedMetric so need to create with the bucket array
        if (isLog(3)) {
          log(3, "... init with buckets", Arrays.toString(buckets));
        }

        push(mv, buckets.length);
        mv.visitIntInsn(NEWARRAY, Opcodes.T_INT);
        for (int j = 0; j < buckets.length; j++) {
          mv.visitInsn(DUP);
          push(mv, j);
          push(mv, buckets[j]);
          mv.visitInsn(IASTORE);
        }
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, isTraced() ? CREATE_TRACED_TIMER : CREATE_TIMER, "(Ljava/lang/String;[I)Lio/avaje/metrics/Timer;", false);
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_" + i, LTIMED_METRIC);
      }
    }
  }

  void addFieldDefinition(ClassVisitor cv, int i) {
    if (isEnhanced()) {
      if (isLog(4)) {
        log(4, "... init field index[" + i + "] METHOD[" + getUniqueMetricName() + "]", "");
      }
      FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_$metric_" + i, LTIMED_METRIC, null, null);
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
