package org.inspect.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "inspectapi", span = Timed.SpanMode.ON)
public class InspectableTaggedSpanTimedResource {

  public String tracedMethod() {
    return "ok";
  }

  @Timed(span = Timed.SpanMode.OFF)
  public String plainMethod() {
    return "plain";
  }

  @Timed(buckets = {100, 200})
  public void tracedBucketMethod() {
  }
}
