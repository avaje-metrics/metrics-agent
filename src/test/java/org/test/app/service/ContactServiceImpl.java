package org.test.app.service;

import java.util.List;

import org.avaje.metric.TimedMetric;
import org.springframework.stereotype.Service;
import org.test.app.model.Contact;

@Service
public class ContactServiceImpl implements ContactService {

    private ContactDataLayer contactDataLayer;
    
    public ContactServiceImpl(ContactDataLayer contactDataLayer) {
        this.contactDataLayer = contactDataLayer;
    }

    static TimedMetric _metric3 = new TimedMetric("foo");

    @Override
    public boolean sendAlert(Contact contact) {
        long start = System.nanoTime();
        try {
            List<Contact> fetchData = contactDataLayer.fetchData();
            return fetchData.isEmpty();
        } finally {
            _metric3.operationEnd(System.nanoTime() - start, 171);
        }
    }

    
}
