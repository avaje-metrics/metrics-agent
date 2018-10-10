package org.test.app.service;

import org.avaje.metric.annotation.Timed;
import org.springframework.stereotype.Service;

@Timed(prefix = "service")
@Service
public class OrderService {

  public void processOrders() throws InterruptedException {

    Thread.sleep(1);
  }
}
