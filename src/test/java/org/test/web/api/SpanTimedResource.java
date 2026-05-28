package org.test.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "spanapi", span = Timed.SpanMode.CHILD)
public class SpanTimedResource {

  public String tracedMethod() {
    return "ok";
  }

  @Timed(span = Timed.SpanMode.OFF)
  public String plainMethod() {
    return "plain";
  }

  @Timed(span = Timed.SpanMode.ROOT)
  public String rootMethod() {
    return "root";
  }

  @Timed(buckets = {100, 200})
  public void tracedBucketMethod() {
  }

  public String tracedError() {
    throw new IllegalStateException("boom");
  }
}
