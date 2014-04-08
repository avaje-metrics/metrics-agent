package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Type;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;

public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private final EnhanceContext context;
  
  private Label startFinally = new Label();
  
  private final String className;
  private final String methodName;
  
  private final int metricIndex;
  private int posTimeStart;
  
  private boolean detectExplicit;
  private boolean detectJaxrs;
  
  private boolean enhanced;
  
  public AddTimerMetricMethodAdapter(EnhanceContext context, boolean publicMethod, String className, int metricIndex, MethodVisitor mv, int acc, String name, String desc) {
    super(ASM4, mv, acc, name, desc);
    this.context = context;
    this.className = className;
    this.methodName = name;
    this.metricIndex = metricIndex;
    this.enhanced = publicMethod;
  }
  
  /**
   * Return true if this method was enhanced.
   */
  public boolean isEnhanced() {
    return enhanced;
  }
  
  public void visitCode() {
    super.visitCode();
    if (detectJaxrs) {
      log(7,"... detected Jaxrs on: "+methodName);
    }
    if (detectExplicit) {
      log(7,"... explicit on: "+methodName);
    }
    if (enhanced) {
      log(4,"... enhancing method: "+methodName);
      mv.visitLabel(startFinally);
    }
  }
  
  private void log(int level, String msg) {
    context.log(level, msg);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    
    AnnotationVisitor av =  super.visitAnnotation(desc, visible);
    
    log(7,"check method annotation "+desc);

    if (desc.equals("Lorg/avaje/metric/annotation/Timed;")) {
      log(4,"found Timed annotation "+desc);
      detectExplicit = true;
      enhanced = true;
      return av;
    }
    
    if (desc.startsWith("Ljavax/ws/rs")) {
      detectJaxrs = true;
      enhanced = true;
      return av;
    }
    
    return av;
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
        mv.visitLdcInsn("Exiting method " + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream", "println","(Ljava/lang/String;)V");
      }
      
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(1, l5);
      mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, ClassAdapterMetric.LCOLLECTOR);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
      loadLocal(posTimeStart);
      mv.visitInsn(LSUB);
      visitIntInsn(SIPUSH, opcode);
      mv.visitMethodInsn(INVOKEVIRTUAL, ClassAdapterMetric.COLLECTOR, ClassAdapterMetric.COLLECTOR_END_METHOD, "(JI)V");
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
