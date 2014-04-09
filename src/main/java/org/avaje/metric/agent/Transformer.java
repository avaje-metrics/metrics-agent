package org.avaje.metric.agent;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.avaje.metric.agent.asm.ClassReader;
import org.avaje.metric.agent.asm.ClassWriter;

/**
 * A Class file Transformer that enhances entity beans.
 * <p>
 * This is used as both a javaagent or via an ANT task (or other off line
 * approach).
 * </p>
 */
public class Transformer implements ClassFileTransformer {

  public static void premain(String agentArgs, Instrumentation inst) {

    Transformer t = new Transformer(agentArgs);
    inst.addTransformer(t);

    if (t.getLogLevel() > 0) {
      System.out.println("premain loading Transformer with args:" + agentArgs);
    }
  }

  public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
    Transformer t = new Transformer( agentArgs);
    inst.addTransformer(t);

    if (t.getLogLevel() > 0) {
      System.out.println("agentmain loading Transformer with args:" + agentArgs);
    }
  }

  private final EnhanceContext enhanceContext;

  public Transformer(String agentArgs) {
    this.enhanceContext = new EnhanceContext(false, agentArgs);
  }

  /**
   * Change the logout to something other than system out.
   */
  public void setLogout(PrintStream logout) {
    this.enhanceContext.setLogout(logout);
  }

  public void log(int level, String msg) {
    enhanceContext.log(level, msg);
  }

  public int getLogLevel() {
    return enhanceContext.getLogLevel();
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

    try {

      // ignore JDK and JDBC classes etc
      if (enhanceContext.isIgnoreClass(className)) {
        enhanceContext.log(8, "ignore class " + className);
        return null;
      }

      enhanceContext.log(8, "look at "+className);
      return enhancement(loader, classfileBuffer);
     
    } catch (NoEnhancementRequiredException e) {
      // the class is an interface
      log(8, "No Enhancement required " + e.getMessage());
      return null;

    } catch (Exception e) {
      // a safety net for unexpected errors
      // in the transformation
      enhanceContext.log(e);
      return null;
    }
  }


  /**
   * Perform enhancement.
   */
  private byte[] enhancement(ClassLoader loader, byte[] classfileBuffer) {

    ClassReader cr = new ClassReader(classfileBuffer);
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    ClassAdapterMetric ca = new ClassAdapterMetric(cw, enhanceContext);

    try {

      cr.accept(ca, ClassReader.EXPAND_FRAMES);
      if (ca.isLog(3)) {
        ca.log("enhanced");
      }

      if (enhanceContext.isReadOnly()) {
        if (ca.isLog(3)) {
          ca.log("readonly mode - not enhanced");
        }
        return null;

      } else {
        return cw.toByteArray();
      }

    } catch (AlreadyEnhancedException e) {
      if (ca.isLog(1)) {
        ca.log("already enhanced");
      }
      return null;

    } catch (NoEnhancementRequiredException e) {
      if (ca.isLog(4)) {
        ca.log("... skipping, no enhancement required");
      }
      return null;
    }
  }

}
