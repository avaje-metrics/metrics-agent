package org.test.web.api;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;

import java.util.stream.Stream;

@Controller
public class StreamResource {

  @Get
  public Stream<String> streamAll() {
    return Stream.of("a", "b", "c");
  }

  @Get
  public String findOne() {
    return "one";
  }

  public Stream<String> publicStreamMethod() {
    return Stream.of("x", "y");
  }

  public String publicNormalMethod() {
    return "normal";
  }
}
