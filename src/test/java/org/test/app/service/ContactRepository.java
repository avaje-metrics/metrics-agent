package org.test.app.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.test.app.model.Contact;

@Repository
public class ContactRepository implements ContactDataLayer {

  @Override
  public List<Contact> fetchData() {
    return Collections.emptyList();
  }

}
