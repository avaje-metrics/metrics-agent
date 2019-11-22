package io.avaje.metrics.agent;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AgentManifestTest {

  @Test
  public void trim() throws IOException {

    AgentManifest manifest = new AgentManifest();
    manifest.readManifests(this.getClass().getClassLoader(), "test-manifest/test-1.mf");

    assertEquals(manifest.trim("junk.Hello"),"junk.Hello");
    assertEquals(manifest.trim("com.foo.bar.web.Hello"),"Hello");
    assertEquals(manifest.trim("com.foo.bar.Hello"),"Hello");
    assertEquals(manifest.trim("com.foo.bar.web.moo.Hello"),"Hello");
    assertEquals(manifest.trim("com.foo.bar.web.moo2.Hello"),"moo2.Hello");
    assertEquals(manifest.trim("com.foo.bar2.Hello"),"bar2.Hello");
  }
}
