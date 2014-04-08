package org.avaje.metric.agent.asm.commons;

import org.avaje.metric.agent.asm.Label;
import org.avaje.metric.agent.asm.MethodVisitor;


public abstract class FinallyAdapter extends AdviceAdapter {

	protected Label startFinally = new Label();

	public FinallyAdapter(MethodVisitor mv, int acc, String name, String desc) {
		super(ASM4, mv, acc, name, desc);
	}

	public void visitCode() {
		super.visitCode();
		mv.visitLabel(startFinally);
	}

	public void visitMaxs(int maxStack, int maxLocals) {

		Label endFinally = new Label();
		mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
		mv.visitLabel(endFinally);
		onFinally(ATHROW);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(maxStack, maxLocals);
	}

	protected final void onMethodExit(int opcode) {
		if (opcode != ATHROW) {
			onFinally(opcode);
		}
	}

	protected abstract void onFinally(int opcode);
	

}
