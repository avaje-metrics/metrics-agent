package com.myapp.something.nice;


import org.avaje.metric.annotation.Timed;

@Timed(buckets = 500)
public class NiceService {

  public void doNice() {
  }
}
