package org.avaje.metric.agent;

import java.util.HashMap;
import java.util.Map;

import org.avaje.metric.agent.asm.ClassReader;

/**
 * Reads class information for super classes.
 * <p>
 * Specifically read super classes as long as they are abstract classes and not Object.
 * We want to see if there are any inherited methods on abstract classes that we should 'proxy'
 * so as to collect statistics that better reflecting what is happening. 
 * </p>
 */
public class ClassMetaReader {

	private Map<String, ClassMeta> cache = new HashMap<String, ClassMeta>();

	private final EnhanceContext enhanceContext;

	public ClassMetaReader(EnhanceContext enhanceContext) {
		this.enhanceContext = enhanceContext;
	}

	public ClassMeta get(String name, ClassLoader classLoader) throws ClassNotFoundException {
		return getWithCache(name, classLoader);
	}

	private ClassMeta getWithCache(String name, ClassLoader classLoader) throws ClassNotFoundException {
		
		synchronized (cache) {
			ClassMeta meta = cache.get(name);
			if (meta == null) {
				meta = readFromResource(name, classLoader);
				if (meta != null) {
					if (meta.isCheckForMethodsToProxy()) {
						ClassMeta superMeta = getWithCache(meta.getSuperClassName(), classLoader);
						if (superMeta != null && superMeta.isCheckForMethodsToProxy()) {
							meta.setSuperMeta(superMeta);
						}
					}
					cache.put(name, meta);
				}
			}
			return meta;
		}
	}

	private ClassMeta readFromResource(String className, ClassLoader classLoader) throws ClassNotFoundException {

		byte[] classBytes = enhanceContext.getClassBytes(className, classLoader);
		if (classBytes == null){
			enhanceContext.log(1, "Class ["+className+"] not found.");
			return null;
		} else {
			if (enhanceContext.isLog(3)) {
				enhanceContext.log(className, "read ClassMeta");
			}
		}
		ClassReader cr = new ClassReader(classBytes);
		ClassMetaReaderVisitor ca = new ClassMetaReaderVisitor(enhanceContext);

		cr.accept(ca, 0);

		return ca.getClassMeta();
	}

}
