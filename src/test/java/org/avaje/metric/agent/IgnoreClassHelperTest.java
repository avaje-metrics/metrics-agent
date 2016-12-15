package org.avaje.metric.agent;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class IgnoreClassHelperTest {

  @Test
  public void isIgnoreClass() throws Exception {

    IgnoreClassHelper ignore = new IgnoreClassHelper(null);
    assertTrue(ignore.isIgnoreClass("java/util"));
    assertTrue(ignore.isIgnoreClass("sun.Foo"));
    assertTrue(ignore.isIgnoreClass("org/joda/Date"));
    assertTrue(ignore.isIgnoreClass("org.joda.Date"));

    assertFalse(ignore.isIgnoreClass("org.example.Foo"));
  }


  @Test
  public void isIgnoreClass_when_explicitList() throws Exception {

    IgnoreClassHelper ignore = new IgnoreClassHelper(Arrays.asList("com.my.pack","org.my.bar"));

    assertTrue(ignore.isIgnoreClass("java/util"));
    assertTrue(ignore.isIgnoreClass("sun.Foo"));
    assertTrue(ignore.isIgnoreClass("org/joda/Date"));
    assertTrue(ignore.isIgnoreClass("com.banana.Date"));

    assertFalse(ignore.isIgnoreClass("com.my.pack.Foo"));
    assertFalse(ignore.isIgnoreClass("com.my.pack.some.Foo"));
    assertFalse(ignore.isIgnoreClass("org.my.bar.Foo"));
    assertFalse(ignore.isIgnoreClass("org.my.bar.some.Foo"));
  }

}