package org.test.web.api;

import org.avaje.metric.agent.offline.MainTransform;

import java.io.IOException;

public abstract class BaseTest {

  static String[] transformArgs = { "./target/test-classes", "org/test/**,com/myapp/**", "readonly=false;debug=9;sysoutoncollect=true" };

  static {
    try {
      MainTransform.main(transformArgs);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
