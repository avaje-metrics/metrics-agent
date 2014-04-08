package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Type;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;
import org.avaje.metric.agent.asm.commons.FinallyAdapter;

public class AddTimerMetricMethodAdapter extends AdviceAdapter {

  private Label startFinally = new Label();
  
  private final String className;
  private final String methodName;
  
  private final int metricIndex;
  private int posTimeStart;
  
  public AddTimerMetricMethodAdapter(String className, int metricIndex, MethodVisitor mv, int acc, String name, String desc) {
    super(ASM4, mv, acc, name, desc);
    this.className = className;
    this.methodName = name;
    this.metricIndex = metricIndex;
  }
  
  public void visitCode() {
    super.visitCode();
    mv.visitLabel(startFinally);
  }
  
  

  @Override
  public void visitMaxs(int maxStack, int maxLocals) {
    
    Label endFinally = new Label();
    mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
    mv.visitLabel(endFinally);
    
    onFinally(ATHROW);
    mv.visitInsn(ATHROW);
    mv.visitMaxs(maxStack, maxLocals);
  }

//  protected void onFinally(int opCode) {
//    visitMethodInsn(INVOKESTATIC, className, "onExit", "(Ljava/lang/Object;I)V");
//  }
  
  private void onFinally(int opcode) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
    mv.visitLdcInsn("Exiting method 3  " + methodName);
    mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream", "println","(Ljava/lang/String;)V");
    
    Label l5 = new Label();
    mv.visitLabel(l5);
    mv.visitLineNumber(1, l5);
    mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, "Lorg/test/main/MetricCollector;");
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
    //mv.visitVarInsn(LLOAD, posTimeStart);
    loadLocal(posTimeStart);
    mv.visitInsn(LSUB);
    //mv.visitIntInsn(BIPUSH, 123);
    visitIntInsn(SIPUSH, opcode);
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/test/main/MetricCollector", "end", "(JI)V");
  }
  
  protected void onMethodExit(int opcode) {
    if(opcode!=ATHROW) {
      onFinally(opcode);
    }
  }
  
  @Override
  protected void onMethodEnter() {
   
    posTimeStart = newLocal(Type.LONG_TYPE);
    
    //mv.visitLineNumber(16, l0);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
    mv.visitVarInsn(LSTORE, posTimeStart);
  }

//   public void onMethodExit(int opcode) {
//     if(opcode==RETURN) {
//         visitInsn(ACONST_NULL);
//     } else if(opcode==ARETURN || opcode==ATHROW) {
//         dup();
//     } else {
//         if(opcode==LRETURN || opcode==DRETURN) {
//             dup2();
//         } else {
//             dup();
//         }
//         box(Type.getReturnType(this.methodDesc));
//     }
//     visitIntInsn(SIPUSH, opcode);
//     visitMethodInsn(INVOKESTATIC, className, "onExit", "(Ljava/lang/Object;I)V");
//   }
  
//  
//  @Override
//  protected void onFinally(int opcode) {
//    
//    if (opcode == RETURN) {
//      visitInsn(ACONST_NULL);
//
//    } else if (opcode == ARETURN || opcode == ATHROW) {
//      dup();
//
//    } else {
//      if (opcode == LRETURN || opcode == DRETURN) {
//        dup2();
//      } else {
//        dup();
//      }
//      box(Type.getReturnType(this.methodDesc));
//    }
//    visitIntInsn(SIPUSH, opcode);
//    
//    
//    Label l5 = new Label();
//    mv.visitLabel(l5);
//    mv.visitLineNumber(1, l5);
//    mv.visitFieldInsn(GETSTATIC, className, "_$metric_"+metricIndex, "Lorg/test/main/MetricCollector;");
//    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//    //mv.visitVarInsn(LLOAD, posTimeStart);
//    loadLocal(posTimeStart);
//    mv.visitInsn(LSUB);
//    mv.visitMethodInsn(INVOKEVIRTUAL, "org/test/main/MetricCollector", "end", "(J)V");
//    
//  }

  
}
