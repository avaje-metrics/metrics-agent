package io.avaje.metrics.agent;

import org.junit.Test;

import java.io.IOException;
import java.util.jar.Attributes;

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

  @Test
  public void timedSpans_unsetDefaultsOff() {

    AgentManifest manifest = new AgentManifest();

    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertEquals(TimedSpanMode.OFF, manifest.timedSpanMode(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.CHILD, TimedSpanMode.DEFAULT));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.ROOT, TimedSpanMode.DEFAULT));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.CHILD));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.ROOT));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.OFF));
  }

  @Test
  public void timedSpans_defaultChildMode() {

    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedSpans", "default-child");

    manifest.readToggles(attributes);

    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertEquals(TimedSpanMode.CHILD, manifest.timedSpanMode(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.CHILD));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.ROOT));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.OFF));
  }

  @Test
  public void timedSpans_defaultOffMode() {

    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedSpans", "default-off");

    manifest.readToggles(attributes);

    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.CHILD));
    assertTrue(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.ROOT));
  }

  @Test
  public void timedSpans_disabledMode() {

    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedSpans", "disabled");

    manifest.readToggles(attributes);

    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.DEFAULT));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.CHILD));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.DEFAULT, TimedSpanMode.ROOT));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.CHILD, TimedSpanMode.CHILD));
    assertFalse(manifest.isTimedSpansEnabled(TimedSpanMode.ROOT, TimedSpanMode.ROOT));
  }

  @Test
  public void timedSpans_defaultOnIsInvalid() {

    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedSpans", "default-on");

    try {
      manifest.readToggles(attributes);
      fail("Expected invalid timedSpans mode");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid timedSpans mode default-on", e.getMessage());
    }
  }

  @Test
  public void timedMetricNaming_unsetDefaultsLabelTag() {
    AgentManifest manifest = new AgentManifest();

    assertTrue(manifest.isTimedMetricNamingLabelTag());
  }

  @Test
  public void timedMetricNaming_labelTagMode() {
    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedMetricNaming", "label-tag");

    manifest.readToggles(attributes);

    assertTrue(manifest.isTimedMetricNamingLabelTag());
  }

  @Test
  public void timedMetricNaming_fullNameMode() {
    AgentManifest manifest = new AgentManifest();
    Attributes attributes = new Attributes();
    attributes.putValue("timedMetricNaming", "full-name");

    manifest.readToggles(attributes);

    assertFalse(manifest.isTimedMetricNamingLabelTag());
  }
}
