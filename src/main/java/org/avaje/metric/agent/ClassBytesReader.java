package org.avaje.metric.agent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementation of ClassBytesReader based on URLClassLoader.
 */
public class ClassBytesReader {
	

	//private final URL[] urls;
	
//	public ClassPathClassBytesReader(URL[] urls) {
//		this.urls = urls == null ? new URL[0]: urls;
//	}
	
	public ClassBytesReader() {
  }
	
	public byte[] getClassBytes(String className, ClassLoader classLoader) {

		//URLClassLoader cl = new URLClassLoader(urls, classLoader);
		try {
  		String resource = className.replace('.', '/') + ".class";
  
  		InputStream is = null;
  		try {
  
  			// read the class bytes, and define the class
  			URL url = classLoader.getResource(resource);
  			if (url == null) {
  				throw new RuntimeException("Class Resource not found for "+resource);
  			}
  	
  			is = url.openStream();
  			byte[] classBytes = readBytes(is);
  
  			return classBytes;
  			
  		} catch (IOException e){
  			throw new RuntimeException("IOException reading bytes for "+className, e);
  			
  		} finally {
  			if (is != null){
  				try {
  					is.close();
  				} catch (IOException e) {
  					throw new RuntimeException("Error closing InputStream for "+className, e);
  				}
  			}
  		}
		} finally {
//		  try {
//        cl.close();
//      } catch (IOException e) {
//        throw new RuntimeException("Error closing URLClassLoader reading bytecode for "+className, e);
//      }
		}
	}
	
	 
  public byte[] readBytes(InputStream is) throws IOException {
    
    BufferedInputStream bis = new BufferedInputStream(is);

    ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

    byte[] buf = new byte[1028];
    
    int len = 0;
    while ((len = bis.read(buf, 0, buf.length)) > -1){
      baos.write(buf, 0, len);
    }
    baos.flush();
    baos.close();
    return baos.toByteArray();
  }
}
