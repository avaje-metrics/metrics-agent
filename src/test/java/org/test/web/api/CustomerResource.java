package org.test.web.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

@Component
@Path("/general")
public class CustomerResource extends BaseResource {

  @GET
  protected String index() {
    return "Hello";
  }
}
