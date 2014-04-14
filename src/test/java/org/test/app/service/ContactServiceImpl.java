package org.test.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.test.app.model.Contact;

@Service
public class ContactServiceImpl implements ContactService {

    private ContactDataLayer contactDataLayer;
    
    public ContactServiceImpl(ContactDataLayer contactDataLayer) {
        this.contactDataLayer = contactDataLayer;
    }
    
    @Override
    public boolean sendAlert(Contact contact) {
        List<Contact> fetchData = contactDataLayer.fetchData();
        
        return fetchData.isEmpty();
    }

    
}
