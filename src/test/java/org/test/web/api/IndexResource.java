package org.test.web.api;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;
import org.test.app.SimpleService;

@Component
//@Path("/")
public class IndexResource {

  private static final Logger log = Logger.getLogger(IndexResource.class.getName());


  //private final SimpleService timeService;

//  @Inject
//  public IndexResource(){//SimpleService timeService) {
////    /this.timeService = timeService;
//    LOGGER.fine("HelloWorldResource() v2");
//  }

//  //@GET
//  //@Produces(MediaType.TEXT_HTML)
//  public String getIndex() {
//    //timeService.doSomething();
//  
//    return String.format("Hello Root url %s: %s", "sdf", "asd");
//  }

//  @GET
//  @Path("/hello")
//  @Produces(MediaType.TEXT_HTML)
//  public String hello() {
//    return String.format("Rob %s: %s", "foo", "bar");
//  }
}