package com.myapp.something.controller;


import io.avaje.metrics.annotation.Timed;

@Timed(prefix = "fooNice")
public class NiceController {

  public void doStuff() {
  }
}
