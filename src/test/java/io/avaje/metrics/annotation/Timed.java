package io.avaje.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker that public methods should have timed execution statistics collected.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {

  enum SpanMode {
    DEFAULT,
    CHILD,
    ROOT,
    OFF
  }

  String prefix() default "";

  String name() default "";

  String[] tags() default {};

  int[] buckets() default {};

  SpanMode span() default SpanMode.DEFAULT;
}
