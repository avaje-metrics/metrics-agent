package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

public class TaggedGenericTimedResource implements GenericTimedHandler<String> {

  @Override
  @Timed(prefix = "bridgeapi", span = Timed.SpanMode.ROOT)
  public String handle(String value) {
    return value;
  }
}
