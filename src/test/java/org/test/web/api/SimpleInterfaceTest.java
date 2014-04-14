package org.test.web.api;

import org.junit.Test;
import org.test.app.model.Contact;
import org.test.app.service.ContactDataLayer;
import org.test.app.service.ContactRepository;
import org.test.app.service.ContactServiceImpl;
import org.test.main.ExampleService;

public class SimpleInterfaceTest extends BaseTest {

    @Test
    public void test() throws InterruptedException {
        
        ContactDataLayer contactDataLayer = new ContactRepository();
        ContactServiceImpl impl = new ContactServiceImpl(contactDataLayer);
        
        impl.sendAlert(new Contact());
        
        ExampleService es = new ExampleService();
        es.doSomething();
        es.delete();
        es.findAll("asd");
        
        Thread.sleep(500);
        System.out.println("done");
    }
}
