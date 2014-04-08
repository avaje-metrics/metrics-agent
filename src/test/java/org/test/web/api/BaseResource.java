package org.test.web.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public abstract class BaseResource {

  @Path("/findall")
  protected String findAll() {
    return "something";
  }
  
}
