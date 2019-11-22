package org.test.app.service;

import io.avaje.metrics.annotation.Timed;
import org.springframework.stereotype.Service;

@Timed(prefix = "service")
@Service
public class OrderService {

  public void processOrders() throws InterruptedException {

    Thread.sleep(1);
  }
}
