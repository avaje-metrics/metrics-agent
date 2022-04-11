package io.avaje.metrics.agent;

import io.avaje.metrics.agent.asm.ClassReader;
import io.avaje.metrics.agent.asm.ClassWriter;
import io.avaje.metrics.agent.asm.Opcodes;
import io.avaje.metrics.agent.common.ClassWriterWithoutClassLoading;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.function.Consumer;


/**
 * A Class file Transformer that enhances entity beans.
 * <p>
 * This is used as both a javaagent or via an ANT task (or other off line approach).
 * </p>
 */
public class Transformer implements ClassFileTransformer {

  public static final int ASM_VERSION = Opcodes.ASM9;

  public static void premain(String agentArgs, Instrumentation inst) {

    Transformer t = new Transformer();
    inst.addTransformer(t);

    if (t.getLogLevel() > 0) {
      System.out.println("premain loading Transformer with args:" + agentArgs);
    }
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    Transformer t = new Transformer();
    inst.addTransformer(t);

    if (t.getLogLevel() > 0) {
      System.out.println("agentmain loading Transformer with args:" + agentArgs);
    }
  }

  private final EnhanceContext enhanceContext;

  /**
   * Construct using the default classBytesReader implementation.
   */
  public Transformer(AgentManifest manifest) {
    this.enhanceContext = new EnhanceContext(manifest);
  }

  /**
   * Construct with metric name mapping pre-loaded (e.g. IDE enhancement).
   */
  public Transformer(ClassLoader classLoader) {
    this.enhanceContext = new EnhanceContext(AgentManifest.read(classLoader));
  }

  /**
   * Construct using the context class loader.
   */
  public Transformer() {
    this.enhanceContext = new EnhanceContext(AgentManifest.read(null));
  }

  /**
   * Set a consumer that is notified with metric names during enhancement.
   * Use this to log the enhanced metrics during maven build etc.
   */
  public void setLogger(Consumer<String> logger) {
    enhanceContext.setLogger(logger);
  }

  /**
   * Change the logout to something other than system out.
   */
  public void setLogout(PrintStream logout) {
    this.enhanceContext.setLogout(logout);
  }

  public void log(int level, String msg, String extra) {
    enhanceContext.log(level, msg, extra);
  }

  public int getLogLevel() {
    return enhanceContext.getLogLevel();
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

    try {
      // ignore JDK and JDBC classes etc
      if (enhanceContext.isIgnoreClass(className)) {
        enhanceContext.log(9, "ignore class ", className);
        return null;
      }

      enhanceContext.log(8, "look at ", className);
      return enhancement(loader, classfileBuffer);

    } catch (NoEnhancementRequiredException e) {
      // the class is an interface
      log(8, "No Enhancement required ", e.getMessage());
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
    ClassWriter cw = new ClassWriterWithoutClassLoading(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS, loader);
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
      if (ca.isLog(9)) {
        ca.log("... skipping, no enhancement required");
      }
      return null;
    } catch (IllegalArgumentException | IllegalStateException e) {
      if (ca.isLog(2)) {
        ca.log("No enhancement on class due to " + e);
      }
      return null;
    }
  }
}
