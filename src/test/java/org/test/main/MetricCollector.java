package org.test.main;

public class MetricCollector {

  private final String name;
  
  public MetricCollector(String name) {
    this.name = name;
  }

  public void end(long nanos) {
    end(nanos, -1);
  }

  public void end(long nanos, boolean success) {
    System.out.println(name+" exe: "+nanos+" success:"+success);
  }
  
  public void end(long nanos, int opCode) {
    System.out.println(name+" exe: "+nanos+" opCode:"+opCode);
  }
  
}
