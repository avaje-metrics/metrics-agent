package org.avaje.metric;

public class DefaultTimedMetric implements TimedMetric {

    private final String name;

    public DefaultTimedMetric(String name) {
        this.name = name;
    }

    // public void end(long nanos) {
    // end(nanos, -1);
    // }
    //
    // public void end(long nanos, boolean success) {
    // System.out.println(name+" exe: "+nanos+" success:"+success);
    // }

    @Override
    public void operationEnd(long nanos, int opCode) {
        System.out.println(name + " exe: " + nanos + " opCode:" + opCode);
    }

}
