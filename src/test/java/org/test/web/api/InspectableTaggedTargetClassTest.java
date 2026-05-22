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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InspectableTaggedTargetClassTest {

  private static final String CLASS_NAME = "org.inspect.web.api.InspectableTaggedSpanTimedResource";
  private static final Path TARGET_TEST_CLASSES = Paths.get("./target/test-classes");

  @Before
  public void before() {
    Metrics.testClear();
  }

  @Test
  public void targetTestClasses_containsTaggedBuilderExamples() throws Exception {
    transformInPlace("org/inspect/web/api", "timedMetricNaming: label-tag");

    try (TransformedClassLoader loader = new TransformedClassLoader(TARGET_TEST_CLASSES, getClass().getClassLoader(), CLASS_NAME)) {
      Class<?> type = loader.loadClass(CLASS_NAME);
      Object target = type.getDeclaredConstructor().newInstance();

      invokeAndAssertPlain(type, target);
      invokeAndAssertTraced(type, target);
      invokeAndAssertBucketedTraced(type, target);
    }
  }

  private void invokeAndAssertPlain(Class<?> type, Object target) throws Exception {
    Metrics.testReset();
    Method method = type.getMethod("plainMethod");
    method.invoke(target);

    assertEquals("inspectapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:InspectableTaggedSpanTimedResource.plainMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasAdd());
    MockTimer metric = Metrics.testGetTimedMetric("inspectapi", "label:InspectableTaggedSpanTimedResource.plainMethod");
    assertNotNull(metric);
    assertFalse(metric.testIsTraced());
  }

  private void invokeAndAssertTraced(Class<?> type, Object target) throws Exception {
    Metrics.testReset();
    Method method = type.getMethod("tracedMethod");
    method.invoke(target);

    assertEquals("inspectapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:InspectableTaggedSpanTimedResource.tracedMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    MockTimer metric = Metrics.testGetTimedMetric("inspectapi", "label:InspectableTaggedSpanTimedResource.tracedMethod");
    assertNotNull(metric);
    assertTrue(metric.testIsTraced());
  }

  private void invokeAndAssertBucketedTraced(Class<?> type, Object target) throws Exception {
    Metrics.testReset();
    Method method = type.getMethod("tracedBucketMethod");
    method.invoke(target);

    assertEquals("inspectapi", Metrics.testLastMetricName());
    assertArrayEquals(new String[]{"label:InspectableTaggedSpanTimedResource.tracedBucketMethod"}, Metrics.testLastMetricTags());
    assertTrue(Metrics.testLastOperationWasEvent());
    MockBucketTimer metric = Metrics.testGetBucketTimedMetric("inspectapi", "label:InspectableTaggedSpanTimedResource.tracedBucketMethod");
    assertNotNull(metric);
    assertTrue(metric.testIsTraced());
  }

  private void transformInPlace(String packageName, String... manifestEntries) throws Exception {
    AgentManifest manifest = manifest(manifestEntries);
    Transformer transformer = new Transformer(manifest);
    OfflineFileTransform fileTransform = new OfflineFileTransform(
      transformer,
      getClass().getClassLoader(),
      TARGET_TEST_CLASSES.toString(),
      TARGET_TEST_CLASSES.toString());
    fileTransform.process(packageName);
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

  private static final class TransformedClassLoader extends URLClassLoader {

    private final String transformedClassName;

    private TransformedClassLoader(Path rootDir, ClassLoader parent, String transformedClassName) throws IOException {
      super(new URL[]{rootDir.toUri().toURL()}, parent);
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
