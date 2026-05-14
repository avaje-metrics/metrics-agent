package org.test.web.api;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.metrics.annotation.Timed;

@Controller("/default-span")
public class DefaultSpanController {

  @Timed(span = Timed.SpanMode.ON)
  @Get("/traced")
  public String tracedEndpoint() {
    return "ok";
  }

  @Get("/plain")
  public String plainEndpoint() {
    return "plain";
  }
}
