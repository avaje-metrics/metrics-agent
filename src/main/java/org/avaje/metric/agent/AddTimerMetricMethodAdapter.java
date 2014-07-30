package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;
import org.avaje.metric.agent.asm.Type;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;

/**
 * Enhances a method adding the TimerMetric collection.
 */
public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private final EnhanceContext context;
  
  private Label startFinally = new Label();
  
  private final String className;
  
  private final String methodName;
  
  private final int metricIndex;
  
  private final String uniqueMethodName;
  
  private String customName;
  
  private String customFullName;
  
  private int posTimeStart;

  private boolean detectNotTimed;  
  
  private boolean enhanced;
  
  public AddTimerMetricMethodAdapter(EnhanceContext context, boolean enhanceDefault, String className, 
      int metricIndex, String uniqueMethodName, MethodVisitor mv, int acc, String name, String desc) {
    
    super(ASM4, mv, acc, name, desc);
    this.context = context;
    this.className = className;
    this.methodName = name;
    this.metricIndex = metricIndex;
    this.uniqueMethodName = uniqueMethodName;
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
  private void setCustomName(String customName) {
    this.customName = customName;
  }

  /**
   * Set by Timed annotation fullName attribute.
   */
  private void setCustomFullName(String customFullName) {
    this.customFullName = customFullName;
  }

  public String getUniqueMethodName() {
    if (customFullName != null && customFullName.trim().length() > 0) {
      return customFullName.trim();
    }
    if (customName != null && customName.trim().length() > 0) {
      return className.replace('/', '.') + "." + customName.trim();
    }
    return uniqueMethodName;
  }
  
  public int getMetricIndex() {
    return metricIndex;
  }

  public String getNameDescription() {
    return methodName+methodDesc;
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
        setCustomName(value.toString());
      }
      if ("fullName".equals(name) && !"".equals(value)) {
        setCustomFullName(value.toString());
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
          log(8,"... add visitFrame in ", uniqueMethodName);
        }
        mv.visitFrame(Opcodes.F_SAME, 1, new Object[]{Opcodes.LONG}, 0, null);
      }
            
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(1, l5);
      mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, ClassAdapterMetric.LCOLLECTOR);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
      loadLocal(posTimeStart);
      mv.visitInsn(LSUB);
      visitIntInsn(SIPUSH, opcode);
      mv.visitMethodInsn(INVOKEINTERFACE, ClassAdapterMetric.COLLECTOR, ClassAdapterMetric.COLLECTOR_END_METHOD, "(JI)V");
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
      posTimeStart = newLocal(Type.LONG_TYPE);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
      mv.visitVarInsn(LSTORE, posTimeStart);
    }
  }


}
