package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "spanapi", span = Timed.SpanMode.CHILD)
public class TaggedSpanTimedResource {

  public String tracedMethod() {
    return "ok";
  }

  @Timed(buckets = {100, 200})
  public void tracedBucketMethod() {
  }
}
