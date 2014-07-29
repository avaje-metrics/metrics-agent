package org.avaje.metric.agent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

/**
 * Reads the bytecode bytes for a given class.
 * <p>
 * Used to read meta data for a class by visiting the raw class bytes.
 */
public class ClassBytesReader {

  private final int logLevel;
    
  private final PrintStream logout;
    
  public ClassBytesReader(int logLevel, PrintStream logout) {
    this.logLevel = logLevel;
    this.logout = logout;
  }

  public byte[] getClassBytes(String className, ClassLoader classLoader) {

    String resource = className.replace('.', '/') + ".class";

    InputStream is = null;
    try {

      // read the class bytes, and define the class
      URL url = classLoader.getResource(resource);
      if (url == null) {
        if (logLevel > 2) {
          logout.println("Class Resource not found for " + resource);
        }
        return null;
      }

      is = url.openStream();
      return readBytes(is);

    } catch (IOException e) {
      if (logLevel > 2) {
        logout.println("IOException reading bytes for " + className + " error:"+ e);
      }
      return null;

    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          throw new RuntimeException("Error closing InputStream for " + className, e);
        }
      }
    }
  }

  /**
   * Read the inputStream returning as a byte array.
   */
  public byte[] readBytes(InputStream is) throws IOException {

    BufferedInputStream bis = new BufferedInputStream(is);

    ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

    byte[] buf = new byte[1028];

    int len = 0;
    while ((len = bis.read(buf, 0, buf.length)) > -1) {
      baos.write(buf, 0, len);
    }
    baos.flush();
    baos.close();
    return baos.toByteArray();
  }
}
