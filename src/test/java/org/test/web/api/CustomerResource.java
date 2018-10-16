package org.test.web.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.avaje.metric.annotation.NotTimed;
import org.avaje.metric.annotation.Timed;
import org.springframework.stereotype.Component;

@Timed(prefix = "web.api")
@Component
@Path("/general")
public class CustomerResource extends BaseResource {

  public static void aStaticMethodNotAnnotated() {

  }

  @Timed(name="staticGeneral")
  public static void aStaticMethodWithTimedAnnotation() {

  }

  @POST
  public String publicMethodWithJaxrs() {
    System.out.println("IN publicMethodWithJaxrs");
    return "Hello";
  }

  public String publicMethodWithJaxrs(String param) {
    System.out.println("IN publicMethodWithJaxrs (String)");
    return "Hello";
  }

  public String publicMethodWithJaxrs(String param, int pos) {
    System.out.println("IN publicMethodWithJaxrs (String,int)");
    return "Hello";
  }

  @NotTimed
  public String publicMethodNotTimed() {
    System.out.println("IN publicMethodNotTimed");
    return "Foo";
  }

  protected String nakedProtectedMethod() {
    System.out.println("IN nakedProtectedMethod");
    System.out.println("... calling myPrivateMethod()");
    myPrivateMethod();
    return "Foo";
  }

  public String nakedPublicMethod() {
    System.out.println("IN nakedPublicMethod");
    myTimedPrivateMethod();
    return "Foo";
  }

  private String myPrivateMethod() {
    System.out.println("IN nakedProtectedMethod");
    return "Foo";
  }

  @Timed
  private String myTimedPrivateMethod() {
    System.out.println("IN myTimedPrivateMethod");
    return "Foo";
  }

  public int hashCode() {
    return 1;
  }

  public String toString() {
    return "bar";
  }

}
