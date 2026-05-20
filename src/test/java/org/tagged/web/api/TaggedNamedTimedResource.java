package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(name = "custom.base")
public class TaggedNamedTimedResource {

  public String defaultLabel() {
    return "ok";
  }

  @Timed(name = "namedMethod")
  public String namedLabel() {
    return "ok";
  }
}
