package org.avaje.metric.agent;

import org.avaje.metric.agent.asm.AnnotationVisitor;
import org.avaje.metric.agent.asm.ClassVisitor;
import org.avaje.metric.agent.asm.MethodVisitor;
import org.avaje.metric.agent.asm.Opcodes;

/**
 * Used by ClassMetaReader to read information about a class.
 * <p>
 * Reads the information by visiting the byte codes rather than using
 * ClassLoader. This gets around issues where the annotations are not seen
 * (silently ignored) if they are not in the class path.
 * </p>
 */
public class ClassMetaReaderVisitor extends ClassVisitor {//implements EnhanceConstants {

  private final EnhanceContext context;
	private final ClassMeta classMeta;
	
	public ClassMetaReaderVisitor(EnhanceContext context) {
		super(Opcodes.ASM4);
		this.context = context;
		this.classMeta = context.createClassMeta();
	}

	public ClassMeta getClassMeta() {
		return classMeta;
	}

	public boolean isLog(int level) {
		return context.isLog(level);
	}

	public void log(int level, String msg) {
	  context.log(level, msg);
	}

	/**
	 * Create the class definition replacing the className and super class.
	 */
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		classMeta.setClassName(access, name, superName);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		
		classMeta.addClassAnnotation(desc);
		return super.visitAnnotation(desc, visible);
	}


	/**
	 * Read the method info.
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

		boolean staticAccess = ((access & Opcodes.ACC_STATIC) != 0);
		
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (staticAccess || "<init>".equals(name)){
		  // Not interested in static methods or constructors
		  return mv;
		}
		return classMeta.createMethodVisitor(mv, access, name, desc, signature, exceptions);
	}

}
