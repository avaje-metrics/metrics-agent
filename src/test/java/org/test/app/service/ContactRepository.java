package org.test.app.service;

import java.util.Collections;
import java.util.List;

import io.avaje.metrics.annotation.Timed;
import org.springframework.stereotype.Repository;
import org.test.app.model.Contact;

@Timed(prefix = "repo")
@Repository
public class ContactRepository implements ContactDataLayer {

  @Override
  public List<Contact> fetchData() {
    return Collections.emptyList();
  }

}
