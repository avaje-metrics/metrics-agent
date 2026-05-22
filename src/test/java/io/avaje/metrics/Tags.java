package io.avaje.metrics;

import java.util.Arrays;

public interface Tags {

  static Tags of(String... rawTags) {
    return new TestTags(rawTags);
  }

  String[] array();

  final class TestTags implements Tags {

    private final String[] rawTags;

    TestTags(String[] rawTags) {
      this.rawTags = Arrays.copyOf(rawTags, rawTags.length);
    }

    @Override
    public String[] array() {
      return Arrays.copyOf(rawTags, rawTags.length);
    }
  }
}
