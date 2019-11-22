package org.test.app;

import io.avaje.metrics.annotation.Timed;

@Timed
public class OtherSimpleService {

  public void sayHi() throws RuntimeException {
    System.out.println("hello there");
  }

  @Timed(buckets={100,200})
  public void saySomethingElse(int workMillis) throws InterruptedException {
    try {
      Thread.sleep(workMillis);
      internal();

    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  private void internal() {
    System.out.println("not very interesting");
  }

}
