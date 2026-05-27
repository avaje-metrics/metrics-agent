# metric-agent

Bytecode transformation agent that can be used to add metric collection

Refer to documentation at http://avaje-metrics.github.io/

Usually the agent is used via maven plugin or tile.

## steps

- Add `@Timed` to classes or methods
- Use `@Timed(tags = {"component:billing", "operation:sync"})` to add stable custom timer tags
- Use `@Timed(span = Timed.SpanMode.ON)` when timed methods should also create spans
- Timed methods do not create spans by default; use `@Timed(span = Timed.SpanMode.ON)` on the class or method to opt in
- Add the maven plugin or tile 
- Configure a metric reporter to report metrics (to local csv file or Collectd etc)

## Maven enhancement

The most common way to perform enhancement is to add the metrics Maven plugin to the
application `pom.xml` under `build` / `plugins`:

```xml
<plugin> <!-- perform avaje metrics enhancement -->
  <groupId>io.avaje.metrics</groupId>
  <artifactId>metrics-maven-plugin</artifactId>
  <version>${avaje-metrics.version}</version>
  <extensions>true</extensions>
</plugin>
```

`@Timed(tags = {...})` uses the same `key:value` values as `Tags.of(...)`. Class-level tags
apply to each enhanced method and method-level tags append to them. Tags are applied when
the enhanced static timer field is initialized, so they should be stable, low-cardinality
values rather than request-specific data.

## Configuration (`metrics.mf`)

Most applications do not need a `metrics.mf` file. Add one only when the defaults need
to be changed, such as limiting package scanning, enabling optional framework detection,
or changing timed span and naming behavior.

The agent reads Java manifest resources named `metrics-common.mf` and then `metrics.mf`
from the classpath. When needed, add `metrics.mf` to the application resources, for
example `src/main/resources/metrics.mf`.

```manifest
Manifest-Version: 1.0
packages: com.example.app.**
timedSpans: default-off
timedMetricNaming: label-tag
spring: true
jaxrs: true

```

When specifying a manifest file, ensure that it has a trailing empty line.

`metrics-common.mf` can be used for shared settings and `metrics.mf` for application
settings. If both are used, repeat scalar settings that must apply in `metrics.mf`;
`packages` and `nameTrimPackages` values accumulate. Boolean options use Java boolean
parsing, so `true` enables the option and any other value is false.

| Option | Values / default | Effect |
| --- | --- | --- |
| `packages` | package list, default empty | Limits enhancement to matching package prefixes. Values split on comma, semicolon, or space. Dotted and slash package names are accepted, with optional `*` or `**` suffixes. |
| `debugLevel` | integer, default `0` | Enables agent debug logging. Higher values produce more detailed enhancement output. |
| `readOnly` | boolean, default `false` | Runs enhancement analysis but does not return transformed bytecode. Useful for diagnostics. |
| `includeStaticMethods` | boolean, default `false` | Includes static methods by default when a class is enhanced. Explicitly `@Timed` static methods are still enhanced. |
| `enhanceSingleton` | boolean, default `true` | Enhances classes with annotations ending in `Singleton`, such as `javax.inject.Singleton`, unless disabled. |
| `enhanceAvajeComponent` | boolean, default `true` | Enhances classes annotated with `io.avaje.inject.Component`, unless disabled. |
| `enhanceNonPrivate` | boolean, default `false` | Includes protected and package-visible methods by default on enhanced classes. Private methods are not included. |
| `spring` | boolean, default `false` | Enables class-level enhancement for Spring stereotype annotations. |
| `jaxrs` | boolean, default `false` | Enables `javax.ws.rs` class and method endpoint detection. |
| `jee` | boolean, default `false` | Enables JEE/Jakarta EJB and web service detection. |
| `nameIncludePackages` | boolean, default `false` | Uses fully qualified class names in generated non-web metric names or label values. |
| `nameTrimPackages` | package list, default empty | Legacy parsed setting. Package prefixes are stored longest-first, but current enhancement does not apply this setting to generated metric names. |
| `timedSpans` | `default-off`, `default-on`, `disabled`; default `default-off` | Controls whether timed methods also create spans. `disabled` turns off timed spans globally, including explicit `@Timed(span = Timed.SpanMode.ON)`. |
| `timedMetricNaming` | `full-name`, `label-tag`; default `full-name` | Controls whether timed metric names use the full generated name or a base metric name with a generated `label:` tag. |

When `packages` is not set, the agent skips known JDK, JDBC, logging, test, and common
library packages and checks the remaining classes. Set `packages` in production
applications to reduce scanning and make the enhancement scope explicit.

Class or method `@Timed` annotations are always explicit enhancement points. Automatic
class-level enhancement can also be driven by singleton/component annotations, Avaje HTTP
controller annotations, and the optional `spring`, `jaxrs`, and `jee` detection flags.

### Timed spans

`timedSpans` supports `default-off`, `default-on`, and `disabled`. If unset, timed spans
default to off. `disabled` turns off timed spans globally, including explicit
`@Timed(span = Timed.SpanMode.ON)` settings, while leaving plain timing metrics enabled.

### Timed metric naming

`timedMetricNaming` supports `full-name` and `label-tag`. If unset, `full-name` is used.

- `full-name` keeps the existing metric naming, for example:
  `web.api.CustomerResource.staticGeneral`
- `label-tag` changes timed metrics to use the base metric name plus a `label:` tag, for example:
  `Metrics.timerBuilder("web.api").tags(Tags.of("label:CustomerResource.staticGeneral")).build()`

In `label-tag` mode:

- class `@Timed(prefix = "...")` becomes the base metric name
- class `@Timed(name = "...")` remains the base metric name
- method `@Timed(name = "...")` becomes the `label:` tag value
- otherwise non-web timed classes default to the base metric name `app.component`
- `nameIncludePackages=true` affects the label value rather than the base metric name
- bucket timers also use the same base metric name plus `label:` tag pattern
- custom `@Timed(tags = {...})` values are preserved and the generated `label:` tag is appended
- avoid specifying custom `label:` tags in this mode because `label:` is reserved for the generated method label
