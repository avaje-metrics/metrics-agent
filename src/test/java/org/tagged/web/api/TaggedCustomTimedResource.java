package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "myapi")
public class TaggedCustomTimedResource {

  public String publicMethodNormal() {
    return "ok";
  }

  @Timed(name = "someRandomName")
  public String publicMethodWithName() {
    return "ok";
  }

  @Timed(name = "myname.fully.defined")
  public String publicMethodWithFullName() {
    return "ok";
  }
}
