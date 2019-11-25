package io.avaje.metrics.agent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrimControllerTest {

  @Test
  public void trim() {

    assertEquals("Hi", TrimController.trim("HiController"));
    assertEquals("Hi", TrimController.trim("HiResource"));

    assertEquals("Foo", TrimController.trim("FooController"));
    assertEquals("F", TrimController.trim("FResource"));

    assertEquals("FooControlle", TrimController.trim("FooControlle"));
    assertEquals("FooControllere", TrimController.trim("FooControllere"));

  }
}