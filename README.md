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

`@Timed(tags = {...})` uses the same `key:value` values as `Tags.of(...)`. Class-level tags
apply to each enhanced method and method-level tags append to them. Tags are applied when
the enhanced static timer field is initialized, so they should be stable, low-cardinality
values rather than request-specific data.

The agent also supports a `timedSpans` manifest setting with values:
`default-off`, `default-on`, and `disabled`.
If unset, timed spans default to off.
`disabled` turns off timed spans globally, including explicit `@Timed(span = Timed.SpanMode.ON)` settings, while leaving plain timing metrics enabled.

The agent also supports a `timedMetricNaming` manifest setting with values:
`full-name` and `label-tag`.
If unset, `full-name` is used.

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
