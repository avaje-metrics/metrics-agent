package io.avaje.metrics.agent;

import io.avaje.metrics.agent.asm.Label;
import io.avaje.metrics.agent.asm.MethodVisitor;
import io.avaje.metrics.agent.asm.Opcodes;
import io.avaje.metrics.agent.asm.commons.AdviceAdapter;

import static io.avaje.metrics.agent.Transformer.ASM_VERSION;

/**
 * Enhance an existing static initialisation block with a call to our static
 * _$initMetrics() method.
 */
public class StaticInitAdapter extends AdviceAdapter {

  private final String className;

  StaticInitAdapter(MethodVisitor mv, int access, String name, String desc, String className) {
    super(ASM_VERSION, mv, access, name, desc);
    this.className = className;
  }

  /**
   * Adds the call to the static _$initMetrics() method.
   */
  @Override
  public void visitCode() {
    super.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(1, l0);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, "_$initMetrics", "()V", false);
  }

}
