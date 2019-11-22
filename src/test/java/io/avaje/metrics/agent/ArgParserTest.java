package io.avaje.metrics.agent;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ArgParserTest {

  @Test
  public void testParse() throws Exception {

    HashMap<String, String> map = ArgParser.parse("debug=7;nameFile=/some/metric-mapping.txt");

    assertEquals(2, map.size());
    assertEquals("7", map.get("debug"));
    assertEquals("/some/metric-mapping.txt", map.get("namefile"));
  }
}