package org.avaje.metric.agent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Reads the bytecode bytes for a given class.
 * <p>
 * Used to read meta data for a class by visiting the raw class bytes.
 */
public class ClassBytesReader {

  public ClassBytesReader() {
  }

  public byte[] getClassBytes(String className, ClassLoader classLoader) {

    String resource = className.replace('.', '/') + ".class";

    InputStream is = null;
    try {

      // read the class bytes, and define the class
      URL url = classLoader.getResource(resource);
      if (url == null) {
        throw new RuntimeException("Class Resource not found for " + resource);
      }

      is = url.openStream();
      return readBytes(is);

    } catch (IOException e) {
      throw new RuntimeException("IOException reading bytes for " + className, e);

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
