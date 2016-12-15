package org.avaje.metric.agent;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class NameMappingTest {

  private NameMapping nameMapping = new NameMapping(NameMappingTest.class.getClassLoader(), null);

  @Test
  public void testGetMappedName() throws Exception {

    assertEquals("junk", nameMapping.getMappedName("org.test.junk"));

    assertEquals("na", nameMapping.getMappedName("orange.truck"));
    assertEquals("na.test", nameMapping.getMappedName("orange.truck.test"));
    assertEquals("na.test.junk", nameMapping.getMappedName("orange.truck.test.junk"));
    assertEquals("web.ctl.NiceController", nameMapping.getMappedName("com.myapp.something.controller.NiceController"));

    NameMapping.Match match = nameMapping.findMatch("com.junk.service.One");
    assertNull(match);

    match = nameMapping.findMatch("com.myapp.service.One");
    assertTrue(match.include);
    assertEquals(1, match.buckets.length);
    assertEquals(500, match.buckets[0]);

    match = nameMapping.findMatch("com.myapp.something.controller.NiceService");
    assertTrue(match.include);
    assertEquals(3, match.buckets.length);
    assertEquals(100, match.buckets[0]);
    assertEquals(300, match.buckets[1]);
    assertEquals(500, match.buckets[2]);

    match = nameMapping.findMatch("com.myapp.something.nice.ExcludeMeService");
    assertFalse(match.include);

    match = nameMapping.findMatch("com.myapp.something.controller.NiceController");
    assertTrue(match.include);
    assertEquals(2, match.buckets.length);
    assertEquals(100, match.buckets[0]);
    assertEquals(500, match.buckets[1]);
  }

  @Test
  public void excludeMethod() {

    assertTrue(nameMapping.matchExcludeMethod("com.myapp.something.nice.ExcludeMeService.foo"));
  }

  @Test
  public void includeClass() {
    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "*.MyResource");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("com.MyResource"));
    assertFalse(nameMapping.matchIncludeClass("MyResource"));
    assertFalse(nameMapping.matchIncludeClass("com.MyResource.Proxy"));
  }

  @Test
  public void includeWildCardClass() {
    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "com.*.res.*Res");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("com.fo.res.MyRes"));
    assertTrue(nameMapping.matchIncludeClass("com.ba.res.OtherRes"));
    assertFalse(nameMapping.matchIncludeClass("com.res.MyRes"));
    assertFalse(nameMapping.matchIncludeClass("com.fo.res.MyRest"));
    assertFalse(nameMapping.matchIncludeClass("com.fo.rest.MyRes"));
    assertFalse(nameMapping.matchIncludeClass("org.fo.res.MyRes"));
  }

  @Test
  public void includeMultiWildCardClass() {

    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "com.*Resource");
    map.put("match.include.2", "com.*Service");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("com.MyResource"));
    assertTrue(nameMapping.matchIncludeClass("com.OtherResource"));
    assertTrue(nameMapping.matchIncludeClass("com.MyService"));
    assertTrue(nameMapping.matchIncludeClass("com.OtherService"));

    assertFalse(nameMapping.matchIncludeClass("com.Data"));
  }

  @Test
  public void includeClassExcludeMethod() {

    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "com.*Resource");
    map.put("match.exclude.1", "com.*Resource.ban");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("com.MyResource"));

    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.ban"));

    assertFalse(nameMapping.matchExcludeMethod("com.MyResource.dec"));
    assertFalse(nameMapping.matchExcludeMethod("com.MyResource.inc"));
  }

  @Test
  public void includeClassExcludeMethods() {

    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "com.*Resource");
    map.put("match.exclude.1", "com.*MyResource.*");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("com.MyResource"));

    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.ban"));
    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.dec"));
    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.inc"));
  }

  @Test
  public void excludeMethods() {

    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.exclude.1", "com.*MyResource*");
    map.put("match.exclude.2", "com.*FooResource.ban");

    TD nameMapping = new TD(map);

    //assertTrue(nameMapping.matchIncludeClass("com.MyResource"));
    //assertFalse(nameMapping.matchIncludeClass("com.FooResource"));

    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.ban"));
    assertTrue(nameMapping.matchExcludeMethod("com.MyResource.dec"));

    assertTrue(nameMapping.matchExcludeMethod("com.MyFooResource.ban"));
    assertFalse(nameMapping.matchExcludeMethod("com.MyFooResource.inc"));
  }


  @Test
  public void buckets() {

    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.buckets.1", "com.*MyResource* | 50,100");
    map.put("match.buckets.2", "com.*Service* | 100, 1000, 5000");

    TD nameMapping = new TD(map);

    NameMapping.Match match = nameMapping.findMatch("com.MyResource.ban");
    assertEquals(match.buckets.length, 2);
    assertEquals(match.buckets[0], 50);

    match = nameMapping.findMatch("com.FooMyResource.inc");
    assertEquals(match.buckets.length, 2);
    assertEquals(match.buckets[0], 50);

    match = nameMapping.findMatch("com.FooService.inc");
    assertEquals(match.buckets.length, 3);
    assertEquals(match.buckets[0], 100);
    assertEquals(match.buckets[1], 1000);
    assertEquals(match.buckets[2], 5000);

    match = nameMapping.findMatch("com.FooData.inc");
    assertNull(match);
  }

  @Test
  public void includeClassWild() {
    Map<String,String> map = new LinkedHashMap<>();
    map.put("match.include.1", "nz.c*Resource");

    TD nameMapping = new TD(map);

    assertTrue(nameMapping.matchIncludeClass("nz/co/foo/MyResource"));
  }


  static class TD extends NameMapping {

    TD(Map<String, String> properties) {
      super(null, properties);
    }

    @Override
    protected Enumeration<URL> getNameMappingResources() throws IOException {
      return Collections.enumeration(Collections.emptySet());
    }
  }
}