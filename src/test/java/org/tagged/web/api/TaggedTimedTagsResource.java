package org.tagged.web.api;

import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "tagapi", tags = {"type:class", "component:billing"})
public class TaggedTimedTagsResource {

  public void classTagged() {
  }

  @Timed(tags = {"operation:sync", "source:method"})
  public void methodTagged() {
  }

  @Timed(buckets = {100, 200}, tags = "operation:bucket")
  public void bucketTagged() {
  }

  @Timed(span = Timed.SpanMode.ON, tags = "operation:trace")
  public void tracedTagged() {
  }
}
