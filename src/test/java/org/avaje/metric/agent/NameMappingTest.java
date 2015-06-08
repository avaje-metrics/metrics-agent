package org.avaje.metric.agent;

import org.junit.Test;

import static org.junit.Assert.*;

public class NameMappingTest {

  @Test
  public void testGetMappedName() throws Exception {

    NameMapping nameMapping = new NameMapping(NameMappingTest.class.getClassLoader());

    assertEquals("junk", nameMapping.getMappedName("org.test.junk"));

    assertEquals("na", nameMapping.getMappedName("orange.truck"));
    assertEquals("na.test", nameMapping.getMappedName("orange.truck.test"));
    assertEquals("na.test.junk", nameMapping.getMappedName("orange.truck.test.junk"));
  }
}