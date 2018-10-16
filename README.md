# metric-agent

Bytecode transformation agent that can be used to add metric collection

Refer to documentation at http://avaje-metric.github.io/

Usually the agent is used via maven plugin or tile.

## steps

- Add `@Timed` to classes or methods
- Add the maven plugin or tile 
- Configure a metric reporter to report metrics (to local csv file or Collectd etc)
