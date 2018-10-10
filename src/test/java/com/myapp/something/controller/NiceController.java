package com.myapp.something.controller;


import org.avaje.metric.annotation.Timed;

@Timed(prefix = "fooNice")
public class NiceController {

  public void doStuff() {
  }
}
