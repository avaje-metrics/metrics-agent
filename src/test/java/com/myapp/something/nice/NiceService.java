package com.myapp.something.nice;


import io.avaje.metrics.annotation.Timed;

@Timed(buckets = 500)
public class NiceService {

  public void doNice() {
  }
}
