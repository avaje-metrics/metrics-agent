package org.avaje.metric;

public interface TimedMetric {

    public void operationEnd(long nanos, int opCode);

}