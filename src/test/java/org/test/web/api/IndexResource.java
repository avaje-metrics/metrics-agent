package org.test.web.api;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.avaje.metrics.annotation.Timed;
import org.test.app.SimpleService;

@Timed
@Path("/")
public class IndexResource {

  private static final Logger log = Logger.getLogger(IndexResource.class.getName());

  private final SimpleService timeService;

  private boolean forceError;

  /**
   * For testing - set to true to simulate error.
   */
  protected void testForceError(boolean forceError) {
    this.forceError = forceError;
  }

  @Inject
  public IndexResource(SimpleService timeService) {
    this.timeService = timeService;
    log.fine("HelloWorldResource() v2");
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {

    timeService.doSomething();
    if (forceError) {
      throw new ResourceException();
    }
    return "index content";
  }

  @GET
  @Path("/hello")
  @Produces(MediaType.TEXT_HTML)
  public String hello() {

    return "hello content";
  }
}
