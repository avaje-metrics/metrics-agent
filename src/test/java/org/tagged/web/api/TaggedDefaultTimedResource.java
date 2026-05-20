package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed
public class TaggedDefaultTimedResource {

  public String defaultMethod() {
    return "ok";
  }
}
