package org.test.web.api;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.MockBucketTimer;
import io.avaje.metrics.MockTimer;
import io.avaje.metrics.agent.AgentManifest;
import io.avaje.metrics.agent.Transformer;
import io.avaje.metrics.agent.offline.OfflineFileTransform;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TaggedMetricNamingTest {

  @Before
  public void before() {
    Metrics.testClear();
  }

  @Test
  public void plainTimer_usesBaseNameAndLabelTag() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedCustomTimedResource", "publicMethodNormal", "timedMetricNaming: label-tag");

    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedCustomTimedResource.publicMethodNormal"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasAdd());
    MockTimer metric = Metrics.testGetTimedMetric("myapi", "label:TaggedCustomTimedResource.publicMethodNormal");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  @Test
  public void methodName_overridesLabelTag() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedCustomTimedResource", "publicMethodWithName", "timedMetricNaming: label-tag");

    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:someRandomName"}, Metrics.testLastMetricTags());
  }

  @Test
  public void methodPrefix_overridesClassPrefix() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedCustomTimedResource", "publicMethodWithMethodPrefix", "timedMetricNaming: label-tag");

    assertEquals("lambda", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedCustomTimedResource.publicMethodWithMethodPrefix"}, Metrics.testLastMetricTags());
  }

  @Test
  public void dottedMethodName_becomesLabelOverride() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedCustomTimedResource", "publicMethodWithFullName", "timedMetricNaming: label-tag");

    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:myname.fully.defined"}, Metrics.testLastMetricTags());
  }

  @Test
  public void className_staysBaseMetricName() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedNamedTimedResource", "defaultLabel", "timedMetricNaming: label-tag");

    assertEquals("custom.base", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedNamedTimedResource.defaultLabel"}, Metrics.testLastMetricTags());

    Metrics.testClear();

    invokeTransformed("org.tagged.web.api.TaggedNamedTimedResource", "namedLabel", "timedMetricNaming: label-tag");

    assertEquals("custom.base", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:namedMethod"}, Metrics.testLastMetricTags());
  }

  @Test
  public void tracedTimer_usesEventPathAndLabelTag() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedSpanTimedResource", "tracedMethod", "timedMetricNaming: label-tag");

    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedSpanTimedResource.tracedMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    MockTimer metric = Metrics.testGetTimedMetric("spanapi", "label:TaggedSpanTimedResource.tracedMethod");
    assertNotNull(metric);
    assertTrue(metric.testIsTraced());
  }

  @Test
  public void nameIncludePackages_changesLabelOnly() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedCustomTimedResource", "publicMethodNormal", "timedMetricNaming: label-tag", "nameIncludePackages: true");

    assertEquals("myapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:org.tagged.web.api.TaggedCustomTimedResource.publicMethodNormal"}, Metrics.testLastMetricTags());
  }

  @Test
  public void bucketTimer_usesBaseNameAndLabelTagInLabelTagMode() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedSpanTimedResource", "tracedBucketMethod", "timedMetricNaming: label-tag");

    assertEquals("spanapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedSpanTimedResource.tracedBucketMethod"}, Metrics.testLastMetricTags());
    MockBucketTimer metric = Metrics.testGetBucketTimedMetric("spanapi", "label:TaggedSpanTimedResource.tracedBucketMethod");
    assertNotNull(metric);
    assertTrue(metric.testIsTraced());
  }

  @Test
  public void defaultNonWebBaseName_isAppComponent() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedDefaultTimedResource", "defaultMethod", "timedMetricNaming: label-tag");

    assertEquals("app.component", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:TaggedDefaultTimedResource.defaultMethod"}, Metrics.testLastMetricTags());
    MockTimer metric = Metrics.testGetTimedMetric("app.component", "label:TaggedDefaultTimedResource.defaultMethod");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  @Test
  public void timedTags_classTagsApplyInFullNameMode() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedTimedTagsResource", "classTagged");

    assertEquals("tagapi.TaggedTimedTagsResource.classTagged", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"type:class", "component:billing"}, Metrics.testLastMetricTags());
    MockTimer metric = Metrics.testGetTimedMetric("tagapi.TaggedTimedTagsResource.classTagged", "type:class", "component:billing");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  @Test
  public void timedTags_methodTagsAppendToClassTagsInFullNameMode() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedTimedTagsResource", "methodTagged");

    assertEquals("tagapi.TaggedTimedTagsResource.methodTagged", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"type:class", "component:billing", "operation:sync", "source:method"}, Metrics.testLastMetricTags());
    MockTimer metric = Metrics.testGetTimedMetric(
      "tagapi.TaggedTimedTagsResource.methodTagged",
      "type:class",
      "component:billing",
      "operation:sync",
      "source:method");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  @Test
  public void timedTags_bucketTimersIncludeCustomTags() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedTimedTagsResource", "bucketTagged");

    assertEquals("tagapi.TaggedTimedTagsResource.bucketTagged", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"type:class", "component:billing", "operation:bucket"}, Metrics.testLastMetricTags());
    MockBucketTimer metric = Metrics.testGetBucketTimedMetric(
      "tagapi.TaggedTimedTagsResource.bucketTagged",
      "type:class",
      "component:billing",
      "operation:bucket");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  @Test
  public void timedTags_tracedTimersIncludeCustomTags() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedTimedTagsResource", "tracedTagged");

    assertEquals("tagapi.TaggedTimedTagsResource.tracedTagged", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"type:class", "component:billing", "operation:trace"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    MockTimer metric = Metrics.testGetTimedMetric(
      "tagapi.TaggedTimedTagsResource.tracedTagged",
      "type:class",
      "component:billing",
      "operation:trace");
    assertNotNull(metric);
    assertTrue(metric.testIsTraced());
  }

  @Test
  public void timedTags_labelTagModeAppendsGeneratedLabel() throws Exception {

    invokeTransformed("org.tagged.web.api.TaggedTimedTagsResource", "methodTagged", "timedMetricNaming: label-tag");

    assertEquals("tagapi", Metrics.testLastMetricName());
    assertArrayEquals(
      new String[]{"type:class", "component:billing", "operation:sync", "source:method", "label:TaggedTimedTagsResource.methodTagged"},
      Metrics.testLastMetricTags());
    MockTimer metric = Metrics.testGetTimedMetric(
      "tagapi",
      "type:class",
      "component:billing",
      "operation:sync",
      "source:method",
      "label:TaggedTimedTagsResource.methodTagged");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  private void invokeTransformed(String className, String methodName, String... manifestEntries) throws Exception {
    Path tempDir = Files.createTempDirectory("metrics-agent-tagged-");
    try {
      copyClassFile(className, tempDir);

      AgentManifest manifest = manifest(manifestEntries);
      Transformer transformer = new Transformer(manifest);
      OfflineFileTransform fileTransform = new OfflineFileTransform(transformer, getClass().getClassLoader(), tempDir.toString(), tempDir.toString());
      fileTransform.process(packageName(className));

      try (TransformedClassLoader loader = new TransformedClassLoader(tempDir, getClass().getClassLoader(), className)) {
        Class<?> type = loader.loadClass(className);
        Metrics.testReset();
        Method method = type.getMethod(methodName);
        Object target = Modifier.isStatic(method.getModifiers()) ? null : type.getDeclaredConstructor().newInstance();
        method.invoke(target);
      }
    } finally {
      deleteDirectory(tempDir);
    }
  }

  private AgentManifest manifest(String... manifestEntries) throws IOException {
    StringBuilder builder = new StringBuilder("Manifest-Version: 1.0\n");
    for (String manifestEntry : manifestEntries) {
      builder.append(manifestEntry).append('\n');
    }
    builder.append('\n');

    AgentManifest manifest = new AgentManifest();
    try (InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8))) {
      manifest.addResource(inputStream);
    }
    return manifest;
  }

  private void copyClassFile(String className, Path tempDir) throws IOException {
    String resourceName = className.replace('.', '/') + ".class";
    Path destination = tempDir.resolve(resourceName);
    Files.createDirectories(destination.getParent());
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class resource " + resourceName);
      }
      Files.copy(inputStream, destination);
    }
  }

  private String packageName(String className) {
    int lastDot = className.lastIndexOf('.');
    return className.substring(0, lastDot).replace('.', '/');
  }

  private void deleteDirectory(Path dir) throws IOException {
    try (Stream<Path> stream = Files.walk(dir)) {
      stream.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      throw e;
    }
  }

  private static final class TransformedClassLoader extends URLClassLoader {

    private final String transformedClassName;

    private TransformedClassLoader(Path tempDir, ClassLoader parent, String transformedClassName) throws IOException {
      super(new URL[]{tempDir.toUri().toURL()}, parent);
      this.transformedClassName = transformedClassName;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (transformedClassName.equals(name)) {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
          try {
            loadedClass = findClass(name);
          } catch (ClassNotFoundException e) {
            loadedClass = super.loadClass(name, false);
          }
        }
        if (resolve) {
          resolveClass(loadedClass);
        }
        return loadedClass;
      }
      return super.loadClass(name, resolve);
    }
  }
}
