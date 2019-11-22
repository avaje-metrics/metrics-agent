package io.avaje.metrics.agent.offline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

import io.avaje.metrics.agent.Transformer;

/**
 * Transforms class files when they are on the file system.
 * <p>
 * Typically run as part of an ANT task rather than when Ebean is running.
 * </p>
 */
public class OfflineFileTransform {

	protected final InputStreamTransform inputStreamTransform;

	protected final String inDir;

	protected final String outDir;

	/**
	 * Note that the inDir and outDir can be the same and typically are. That
	 * is, we enhance the class file and replace the file with the the enhanced
	 * version of the class.
	 *
	 * @param transformer
	 *            object that actually transforms the class bytes
	 * @param classLoader
	 *            the ClassLoader used as part of the transformation
	 * @param inDir
	 *            the root directory where the class files are located
	 *
	 * @param outDir
	 *            the root directory where the enhanced files are written to
	 */
	public OfflineFileTransform(Transformer transformer, ClassLoader classLoader, String inDir, String outDir) {

	  this.inputStreamTransform = new InputStreamTransform(transformer, classLoader);
		this.inDir = trimSlash(inDir);
		this.outDir = outDir == null ? inDir : outDir;
	}

	private String trimSlash(String dir) {
		if (dir.endsWith("/")){
			return dir.substring(0, dir.length()-1);
		} else {
			return dir;
		}
	}

	/**
	 * Process all the comma delimited list of packages.
	 * <p>
	 * Package names are effectively converted into a directory on the file
	 * system, and the class files are found and processed.
	 * </p>
	 */
	public void process(String packageNames) throws IOException {

		if (packageNames == null) {
			processPackage("", true);
			return;
		}

		String[] pkgs = packageNames.split(",");
		for (int i = 0; i < pkgs.length; i++) {

			String pkg = pkgs[i].trim().replace('.', '/');

			boolean recurse = false;
			if (pkg.endsWith("**")) {
				recurse = true;
				pkg = pkg.substring(0, pkg.length() - 2);
			} else if (pkg.endsWith("*")) {
				recurse = true;
				pkg = pkg.substring(0, pkg.length() - 1);
			}

			pkg = trimSlash(pkg);

			processPackage(pkg, recurse);
		}
	}

	private void processPackage(String dir, boolean recurse) throws IOException {

		inputStreamTransform.log(5, "transform pkg: ", dir);

		String dirPath = inDir + "/" + dir;
		File d = new File(dirPath);
		if (!d.exists()) {
			throw new FileNotFoundException("File not found " + dirPath);
		}

		File[] files = d.listFiles();

		File file = null;

		try {
      for (int i = 0; i < files.length; i++) {
        file = files[i];
        if (file.isDirectory()) {
          if (recurse) {
            String subdir = dir + "/" + file.getName();
            processPackage(subdir, true);
          }
        } else {
          String fileName = file.getName();
          if (fileName.endsWith(".java")) {
            // possibly a common mistake... mixing .java and .class
            System.err.println("Expecting a .class file but got " + fileName + " ... ignoring");

          } else if (fileName.endsWith(".class")) {
            transformFile(file);
          }
        }
      }

		} catch (IllegalClassFormatException e) {
			throw new IOException("Error transforming file: " + file.getName(), e);
		}

	}

	private void transformFile(File file) throws IOException, IllegalClassFormatException {

		String className = getClassName(file);

		byte[] result = inputStreamTransform.transform(className, file);

		if (result != null) {
			InputStreamTransform.writeBytes(result, file);
		}
	}

	private String getClassName(File file) {

		String path = file.getPath();
		path = path.substring(inDir.length() + 1);
		path = path.substring(0, path.length() - ".class".length());
		// for windows... replace the
		return StringReplace.replace(path,"\\", "/");
	}
}
