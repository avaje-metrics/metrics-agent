package io.avaje.metrics.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used as a marker for classes that are enhanced
 * with timed metrics.
 * <p>
 * This annotation is added to enhanced classes.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricsEnhanced {

}
