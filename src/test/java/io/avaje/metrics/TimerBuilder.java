package io.avaje.metrics;

public interface TimerBuilder {

  TimerBuilder tags(Tags tags);

  TimerBuilder bucketRanges(int... bucketRangesMillis);

  Timer build();

  Timer buildTraced();
}
