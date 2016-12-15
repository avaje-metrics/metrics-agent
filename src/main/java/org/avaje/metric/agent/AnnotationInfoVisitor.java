package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.Opcodes;

/**
 * Reads the annotation information storing it in a AnnotationInfo.
 */
public class AnnotationInfoVisitor extends AnnotationVisitor {
	
	private final AnnotationInfo info;
		
	public AnnotationInfoVisitor(AnnotationInfo info) {
	  super(Opcodes.ASM5);
		this.info = info;
	}
	
	public void visit(String name, Object value) {
		info.add(name, value);
	}
	
}
