package org.avaje.metric.agent;

import org.junit.Test;

import static org.junit.Assert.*;

public class NameMappingTest {

  NameMapping nameMapping = new NameMapping(NameMappingTest.class.getClassLoader());

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
}