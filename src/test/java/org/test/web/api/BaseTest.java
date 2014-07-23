package org.test.web.api;

import org.avaje.metric.agent.offline.MainTransform;

public abstract class BaseTest {

  static String[] transformArgs = { "./target/test-classes", "org/test/**",
      "readonly=false;debug=9;sysoutoncollect=true" };

  static {
    MainTransform.main(transformArgs);
  }

}
