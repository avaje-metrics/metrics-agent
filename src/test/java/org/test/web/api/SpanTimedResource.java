package org.test.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "spanapi", span = Timed.SpanMode.ON)
public class SpanTimedResource {

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

  public String tracedError() {
    throw new IllegalStateException("boom");
  }
}
