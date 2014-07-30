package org.avaje.metric.agent;

//import org.avaje.metric.agent.asm.ClassVisitor;
//import org.avaje.metric.agent.asm.Label;
//import org.avaje.metric.agent.asm.MethodVisitor;
//import org.avaje.metric.agent.asm.Opcodes;
//import org.avaje.metric.agent.asm.Type;

public class AddProxyMethodVisitor {//implements Opcodes {

//  private final MethodMeta methodMeta;
//  
//  private final String className;
//  
//  private final String superClass;
//    
//  public AddProxyMethodVisitor(MethodMeta methodMeta, String className, String superClass) {
//    this.methodMeta = methodMeta;
//    this.className = className;
//    this.superClass = superClass;
//  }
//
//  public void startX(ClassVisitor cw, int fieldIndex) {
//    
//    MethodVisitor mv = methodMeta.createMethodVisitor(cw);
//  
//    mv.visitCode();
//    Label l0 = new Label();
//    Label l1 = new Label();
//    mv.visitTryCatchBlock(l0, l1, l1, null);
//    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//    mv.visitVarInsn(LSTORE, 1);
//    mv.visitLabel(l0);
//    mv.visitLineNumber(1, l0);
//    mv.visitVarInsn(ALOAD, 0);
//    
//    Type[] argumentTypes = Type.getArgumentTypes(methodMeta.getDesc());
//    for (int i = 0; i < argumentTypes.length; i++) {//
//      mv.visitVarInsn(argumentTypes[i].getOpcode(ILOAD), i+1);
//    }
//    
//    mv.visitMethodInsn(INVOKESPECIAL, superClass, methodMeta.getName(), methodMeta.getDesc());
//    
//    //mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
//    //mv.visitLdcInsn("Exiting proxy method "+methodMeta.getName());
//    //mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
//
//    Label l2 = new Label();
//    mv.visitLabel(l2);
//    mv.visitLineNumber(1, l2);
//    mv.visitFieldInsn(GETSTATIC, className, "_$metricP_"+fieldIndex, "Lorg/avaje/metric/TimedMetric;");
//    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//    mv.visitVarInsn(LLOAD, 1);
//    mv.visitInsn(LSUB);
//    mv.visitIntInsn(SIPUSH, 176);
//    mv.visitMethodInsn(INVOKEINTERFACE, "org/avaje/metric/TimedMetric", "operationEnd", "(JI)V");
//    mv.visitInsn(ARETURN);
//    mv.visitLabel(l1);
//    mv.visitFrame(Opcodes.F_FULL, 2, new Object[]{className, Opcodes.LONG}, 1, new Object[]{"java/lang/Throwable"});
//    //mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
//    //mv.visitLdcInsn("Exiting proxy method "+methodMeta.getName());
//    //mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
//    Label l3 = new Label();
//    mv.visitLabel(l3);
//    mv.visitLineNumber(1, l3);
//    mv.visitFieldInsn(GETSTATIC, className, "_$metricP_"+fieldIndex, "Lorg/avaje/metric/TimedMetric;");
//    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//    mv.visitVarInsn(LLOAD, 1);
//    mv.visitInsn(LSUB);
//    mv.visitIntInsn(SIPUSH, 191);
//    mv.visitMethodInsn(INVOKEINTERFACE, "org/avaje/metric/TimedMetric", "operationEnd", "(JI)V");
//    mv.visitInsn(ATHROW);
//    mv.visitLocalVariable("this", "L"+className+";", null, l0, l1, 0);
//    
//    for (int i = 0; i < argumentTypes.length; i++) {
//      //mv.visitVarInsn(argumentTypes[i].getOpcode(ILOAD), i+1);
//      String descriptor = argumentTypes[i].getDescriptor();
//      //String cn = argumentTypes[i].getClassName().replace('.', '/');
//      mv.visitLocalVariable("a"+i, descriptor, null, l0, l1, 0);
//    }
//    
//    mv.visitMaxs(0,0);//6, 3);
//    mv.visitEnd();
//  }
//
//  public void start(ClassVisitor cw, int fieldIndex) {
//    
//    MethodVisitor mv = methodMeta.createMethodVisitor(cw);
//    
//    String name = methodMeta.getName();
//    String desc = methodMeta.getDesc();
//    
//    //mv = cw.visitMethod(methodMeta.getAccess(), name, desc, null, null);
////    {
////        av0 = mv.visitAnnotation("Ljavax/ws/rs/Path;", true);
////        av0.visit("value", "/findall");
////        av0.visitEnd();
////    }
//
//    Type returnType = Type.getReturnType(methodMeta.getDesc());
//    boolean returnVoid = (returnType.getSize() == 0);
//    Type[] argumentTypes = Type.getArgumentTypes(methodMeta.getDesc());
//    
//    int timePos = argumentTypes.length + 1;//2
//    int retPos = timePos + 2; //4
//    int throwPos = retPos + 1; //5
//
//    int nLocal = argumentTypes.length + 2;
//    
//    mv.visitCode();
//    Label l0 = new Label();
//    Label l1 = new Label();
//    Label l2 = new Label();
//    mv.visitTryCatchBlock(l0, l1, l2, null);
//    Label l3 = new Label();
//    mv.visitTryCatchBlock(l2, l3, l2, null);
//    Label l4 = new Label();
//    mv.visitLabel(l4);
//    mv.visitLineNumber(83, l4);
//    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//    mv.visitVarInsn(LSTORE, timePos);
//    mv.visitLabel(l0);
//    mv.visitLineNumber(85, l0);
//    mv.visitVarInsn(ALOAD, 0);
//    
//  //mv.visitVarInsn(ALOAD, 1);
//    
//    for (int i = 0; i < argumentTypes.length; i++) {
//      mv.visitVarInsn(argumentTypes[i].getOpcode(ILOAD), i+1);
//    }
//    
//    
//    mv.visitMethodInsn(INVOKESPECIAL, superClass, name, desc);
//    if (!returnVoid) {
//      mv.visitVarInsn(returnType.getOpcode(IRETURN), retPos); //??
//    }
//    
//    mv.visitLabel(l1);
//    mv.visitLineNumber(87, l1);
//    mv.visitFieldInsn(GETSTATIC, className, "_$metricP_"+fieldIndex, "Lorg/avaje/metric/TimedMetric;");
//    mv.visitVarInsn(LLOAD, timePos);//2);
//    mv.visitIntInsn(SIPUSH, 176);
//    mv.visitMethodInsn(INVOKEINTERFACE, "org/avaje/metric/TimedMetric", "operationEnd", "(JI)V");
//    
//    Label l6 = new Label();
//    
//    if (!returnVoid) {
//      //mv.visitVarInsn(ALOAD, retPos); //??
//      mv.visitVarInsn(returnType.getOpcode(ILOAD), retPos); //??
//      //mv.visitInsn(ARETURN);
//      mv.visitInsn(returnType.getOpcode(IRETURN));
//    } else {
//      mv.visitJumpInsn(GOTO, l6);
//    }
//
//    
//    mv.visitLabel(l2);
//    Object[] a0 = new Object[nLocal];
//    a0[0] = className;
//    for (int i = 0; i < argumentTypes.length; i++) {
//      a0[i+1] = argumentTypes[i].getDescriptor();
//    }
//    a0[nLocal-1] = Opcodes.LONG;
//    
//    //new Object[]{"org/test/web/api/CustomerResource", "java/lang/Long", "java/lang/String", Opcodes.LONG}
//    mv.visitFrame(Opcodes.F_FULL, nLocal, a0, 1, new Object[]{"java/lang/Throwable"});
//    mv.visitVarInsn(ASTORE, throwPos);
//    
//    mv.visitLabel(l3);
//    mv.visitFieldInsn(GETSTATIC, className, "_$metricP_"+fieldIndex, "Lorg/avaje/metric/TimedMetric;");
//    
//    mv.visitVarInsn(LLOAD, timePos);
//    mv.visitIntInsn(SIPUSH, 176);// not 176
//    mv.visitMethodInsn(INVOKEINTERFACE, "org/avaje/metric/TimedMetric", "operationEnd", "(JI)V");
//    mv.visitVarInsn(ALOAD, throwPos);
//    mv.visitInsn(ATHROW);
//
//    Label l5 = new Label();
//    mv.visitLabel(l5);
//
//    if (returnVoid) {
//      mv.visitLabel(l6);
//      //mv.visitLineNumber(93, l6);
//      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//      mv.visitInsn(RETURN);
//    }
//    
//    mv.visitLocalVariable("this", "L"+className+";", null, l4, l5, 0);
//    
//    //mv.visitLocalVariable("orderBy", "Ljava/lang/String;", null, l4, l5, 1);
//    for (int i = 0; i < argumentTypes.length; i++) { 
//      String descriptor = argumentTypes[i].getDescriptor();
//      mv.visitLocalVariable("a"+i, descriptor, null, l0, l1, i+1);
//    }
//    
//    mv.visitLocalVariable("start", "J", null, l0, l5, timePos);
//    mv.visitMaxs(4, 6);
//    mv.visitEnd();
//
//  }
  
}
