# Metrics

The system operator metrics page (`/system-operator/metrics`) shows a single summary list combining:

- **Service usage metrics** — landlord/property registration counts and time-to-first-property
  percentiles, from the database (`MetricsService`).
- **Journey completion rates** — from the Plausible Stats API (`PlausibleMetricsService`, see
  [AnalyticsReadMe](AnalyticsReadMe.md)).
- **Infrastructure metrics** — from Amazon CloudWatch (`CloudWatchMetricsService`), described below.

## CloudWatch infrastructure metrics

`CloudWatchMetricsService` fetches the following six metrics for the selected date range via
`CloudWatchMetricsClient.getMetricStatistic`:

| Dashboard row                 | Namespace             | Metric             | Statistic | Dimensions                              | Region      |
|-------------------------------|-----------------------|--------------------|-----------|-----------------------------------------|-------------|
| Peak memory utilisation       | `ECS/ContainerInsights` | `MemoryUtilization` | Maximum   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| Average memory utilisation    | `ECS/ContainerInsights` | `MemoryUtilization` | Average   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| Peak CPU utilisation          | `ECS/ContainerInsights` | `CPUUtilization`    | Maximum   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| ElastiCache CPU utilisation   | `AWS/ElastiCache`       | `CPUUtilization`    | Maximum   | `CacheClusterId`                        | `eu-west-2` |
| Client error rate (HTTP 4xx)  | `AWS/CloudFront`        | `4xxErrorRate`      | Average   | `DistributionId`, `Region=Global`       | `us-east-1` |
| Server error rate (HTTP 5xx)  | `AWS/CloudFront`        | `5xxErrorRate`      | Average   | `DistributionId`, `Region=Global`       | `us-east-1` |

> **CloudFront is special.** CloudFront only publishes its metrics to **`us-east-1`** with a
> `Region=Global` dimension, so they are queried through a separate `us-east-1` CloudWatch client
> (`cloudFrontCloudWatchClient` in `CloudWatchConfig`). All other metrics use the application's
> configured region (`eu-west-2`).

If a metric returns no data (or any AWS call fails) the service logs
`Failed to fetch CloudWatch metrics: ...` and the affected row renders **"No data"** rather than
erroring the page.

## Client selection by profile

There are two implementations of `CloudWatchMetricsClient`, selected by Spring profile so that
exactly one bean is active at a time:

| Implementation                 | Profile expression | Behaviour                                          |
|--------------------------------|--------------------|----------------------------------------------------|
| `StubCloudWatchMetricsClient`  | `local`            | Returns fixed stub values — **the local default**. |
| `AwsCloudWatchMetricsClient`   | `!local`           | Calls real CloudWatch via the AWS SDK.             |

So when you run locally you always get the stub, and **no AWS credentials are required**. Real
CloudWatch is only called in deployed (non-`local`) environments.
