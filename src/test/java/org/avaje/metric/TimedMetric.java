package org.avaje.metric;

public class TimedMetric {

  private final String name;
  
  public TimedMetric(String name) {
    this.name = name;
  }

//  public void end(long nanos) {
//    end(nanos, -1);
//  }
//
//  public void end(long nanos, boolean success) {
//    System.out.println(name+" exe: "+nanos+" success:"+success);
//  }
  
  public void operationEnd(long nanos, int opCode) {
    System.out.println(name+" exe: "+nanos+" opCode:"+opCode);
  }
  
}
