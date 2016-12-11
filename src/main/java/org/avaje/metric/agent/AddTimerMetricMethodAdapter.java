package org.avaje.metric.agent;

import java.util.Arrays;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.FieldVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;
import org.avaje.metric.agent.asm.Type;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;

/**
 * Enhances a method adding support for using TimerMetric or BucketTimerMetric to collect method
 * execution time.
 */
public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private static final String TIMED_METRIC = "org/avaje/metric/TimedMetric";
  
  private static final String LTIMED_METRIC = "Lorg/avaje/metric/TimedMetric;";

  private static final String BUCKET_TIMED_METRIC = "org/avaje/metric/BucketTimedMetric";
  
  private static final String LBUCKET_TIMED_METRIC = "Lorg/avaje/metric/BucketTimedMetric;";

  public static final String METRIC_MANAGER = "org/avaje/metric/MetricManager";

  public static final String METHOD_OPERATION_END = "operationEnd";

  public static final String METHOD_IS_ACTIVE_THREAD_CONTEXT = "isActiveThreadContext";

  private final ClassAdapterMetric classAdapter;
  
  private final EnhanceContext context;
  
  private Label startFinally = new Label();
  
  private final String className;
  
  private final String methodName;
  
  private final int metricIndex;
  
  private String metricName;
  
  private String metricFullName;
  
  private int[] buckets;

  private int posUseContext;

  private int posTimeStart;

  private boolean detectNotTimed;  
  
  private boolean enhanced;
  
  public AddTimerMetricMethodAdapter(ClassAdapterMetric classAdapter, boolean enhanceDefault,
      int metricIndex, String uniqueMethodName, MethodVisitor mv, int acc, String name, String desc) {
    
    super(ASM4, mv, acc, name, desc);
    this.classAdapter = classAdapter;
    this.context = classAdapter.enhanceContext;
    this.className = classAdapter.className;
    this.methodName = name;
    this.metricIndex = metricIndex;
    this.metricName = uniqueMethodName;
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
  private void setMetricName(String metricName) {
    metricName = metricName.trim();
    if (metricName.length() > 0) {
      this.metricName = metricName;
    }
  }

  /**
   * Set by Timed annotation fullName attribute.
   */
  private void setMetricFullName(String metricFullName) {
    this.metricFullName = metricFullName;
  }
  
  /**
   * Set the bucket ranges to use for this metric/method.
   */
  private void setBuckets(Object bucket) {
    this.buckets = (int[])bucket;
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
  public boolean hasBuckets() {
    return buckets != null && buckets.length > 0 || classAdapter.hasBuckets();
  }

  /**
   * Get the TimeMetric or BucketTimedMetric type.
   */
  private String getMetricType() {
    return hasBuckets() ? BUCKET_TIMED_METRIC: TIMED_METRIC;
  }
  
  /**
   * Get the TimeMetric or BucketTimedMetric type.
   */
  private String getLMetricType() {
    return hasBuckets() ? LBUCKET_TIMED_METRIC: LTIMED_METRIC;
  }
  
  /**
   * Get the unique metric name.
   */
  public String getUniqueMetricName() {
    if (metricFullName != null && metricFullName.trim().length() > 0) {
      return metricFullName.trim();
    }
    return classAdapter.getMetricFullName() + "." + metricName.trim();
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
    
    AnnotationVisitor av =  super.visitAnnotation(desc, visible);
    if (detectNotTimed) {
      // just ignore 
      return av;
    }
    
    if (isLog(7)) {
      log(7,"... check method annotation ", desc);
    }
    if (AnnotationInfo.isNotTimed(desc)) {
      // definitely do not enhance this method
      log(4,"... found NotTimed", desc);
      detectNotTimed = true;
      enhanced = false;
      return av;
    }
    
    if (AnnotationInfo.isTimed(desc)) {
      log(4,"... found Timed annotation ", desc);
      enhanced = true;
      return new TimedAnnotationVisitor(av);
    }

    if (AnnotationInfo.isPostConfigured(desc)) {
      log(4,"... found postConfigured annotation ", desc);
      detectNotTimed = true;
      enhanced = false;
      return av;
    }

    if (AnnotationInfo.isJaxrsEndpoint(desc)) {
      log(4,"... found jaxrs annotation ", desc);
      enhanced = true;
      return av;
    }
    
    return av;
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
      if (context.isSysoutOnCollect()) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("... exiting method " + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream", "println","(Ljava/lang/String;)V");
      }
      
      if (opcode == ATHROW) {
        if (isLog(8)) {
          log(8,"... add visitFrame in ", metricName);
        }
        mv.visitFrame(Opcodes.F_SAME, 1, new Object[]{Opcodes.LONG}, 0, null);
      }
            
      // load opcode
      // load startNanos
      // call interface method operationEnd(opCode, startNanos)
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(1, l5);
      mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, getLMetricType());
      visitIntInsn(SIPUSH, opcode);
      loadLocal(posTimeStart);
      loadLocal(posUseContext);
      mv.visitMethodInsn(INVOKEINTERFACE, getMetricType(), METHOD_OPERATION_END, "(IJZ)V");
    }
  }
  
  protected void onMethodExit(int opcode) {
    if(opcode!=ATHROW) {
      onFinally(opcode);
    }
  }
  
  @Override
  protected void onMethodEnter() {
    if (enhanced) {
      posUseContext = newLocal(Type.BOOLEAN_TYPE);
      mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, getLMetricType());
      mv.visitMethodInsn(INVOKEINTERFACE, getMetricType(), METHOD_IS_ACTIVE_THREAD_CONTEXT, "()Z");
      mv.visitVarInsn(ISTORE, posUseContext);
      posTimeStart = newLocal(Type.LONG_TYPE);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
      mv.visitVarInsn(LSTORE, posTimeStart);
    }
  }
  

  public void addFieldInitialisation(MethodVisitor mv, int i) {
    

    if (!isEnhanced()) {
      log(2, "--- not enhanced (maybe protected/private) ", methodName);

    } else {
      // apply any metric name mappings to the uniqueMethodName to get
      // the final metric name that will be used
      String uniqueMethodName = getUniqueMetricName();
      String mappedMetricName = context.getMappedName(uniqueMethodName);
      if (isLog(1)) {
        if (mappedMetricName.equals(uniqueMethodName)) {
          log(1, "# Add Metric[" + mappedMetricName + "] index[" + i + "]", "");

        } else {
          log(1, "# Add Metric[" + mappedMetricName + "] Method[" + uniqueMethodName + "] index[" + i + "]", "");            
        }
      }

      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(1, l0);
      mv.visitLdcInsn(mappedMetricName);
      
      int[] buckets = getBuckets();
      if (buckets == null || buckets.length == 0) {
        // A TimedMetric
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, "getTimedMetric", "(Ljava/lang/String;)Lorg/avaje/metric/TimedMetric;");
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
        mv.visitMethodInsn(INVOKESTATIC, METRIC_MANAGER, "getTimedMetric", "(Ljava/lang/String;[I)Lorg/avaje/metric/BucketTimedMetric;");
        mv.visitFieldInsn(PUTSTATIC, className, "_$metric_"+i, LBUCKET_TIMED_METRIC);
      }
    }
  }

  public void addFieldDefinition(ClassVisitor cv, int i) {
    if (isEnhanced()) {
      if (isLog(4)) {
        log(4, "... init field index[" + i + "] METHOD[" + getUniqueMetricName() + "]","");
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
