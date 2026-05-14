# metric-agent

Bytecode transformation agent that can be used to add metric collection

Refer to documentation at http://avaje-metrics.github.io/

Usually the agent is used via maven plugin or tile.

## steps

- Add `@Timed` to classes or methods
- Use `@Timed(span = Timed.SpanMode.ON)` when timed methods should also create spans
- Timed methods do not create spans by default; use `@Timed(span = Timed.SpanMode.ON)` on the class or method to opt in
- Add the maven plugin or tile 
- Configure a metric reporter to report metrics (to local csv file or Collectd etc)

The agent also supports a `timedSpans` manifest setting with values:
`default-off`, `default-on`, and `disabled`.
If unset, timed spans default to off.
`disabled` turns off timed spans globally, including explicit `@Timed(span = Timed.SpanMode.ON)` settings, while leaving plain timing metrics enabled.
