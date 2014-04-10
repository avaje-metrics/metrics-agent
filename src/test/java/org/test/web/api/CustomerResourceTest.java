package org.test.web.api;

import org.junit.Test;

public class CustomerResourceTest extends BaseTest {

  @Test
  public void testCustomerResource() {
    
    CustomerResource customerResource = new CustomerResource();
    
    customerResource.publicMethodNotTimed();
    customerResource.publicMethodWithJaxrs();
    customerResource.protectedMethodWithJaxrs();
    customerResource.nakedProtectedMethod();
    customerResource.nakedPublicMethod();
    customerResource.publicMethodWithJaxrs("asd");
    customerResource.publicMethodWithJaxrs("asd", 3);
    customerResource.findAll("ok");
    customerResource.delete();
    customerResource.deleteX(23L, "as");//();
//    customerResource.deleteNone();
    
    customerResource.hashCode();
    customerResource.toString();
    
//    customerResource.findAll("throw");
  }
}
