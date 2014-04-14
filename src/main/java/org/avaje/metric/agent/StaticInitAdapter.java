package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.commons.AdviceAdapter;

/**
 * Enhance an existing static initialisation block with a call to our static
 * _$initMetrics() method.
 */
public class StaticInitAdapter extends AdviceAdapter {

  private final String className;

  protected StaticInitAdapter(MethodVisitor mv, int access, String name, String desc, String className) {
    super(ASM4, mv, access, name, desc);
    this.className = className;
  }

  @Override
  public void visitCode() {
    super.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(1, l0);
    mv.visitMethodInsn(INVOKESTATIC, className, "_$initMetrics", "()V");
  }

}