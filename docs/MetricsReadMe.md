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

| Implementation                 | Profile expression        | Behaviour                                          |
|--------------------------------|---------------------------|----------------------------------------------------|
| `StubCloudWatchMetricsClient`  | `local & !use-cloudwatch` | Returns fixed stub values — **the local default**. |
| `AwsCloudWatchMetricsClient`   | `!local \| use-cloudwatch` | Calls real CloudWatch via the AWS SDK.             |

So when you run locally you get the stub, and **no AWS credentials are required**. You only need to
talk to real CloudWatch when you explicitly add the `use-cloudwatch` profile.

## Running against real CloudWatch locally

By default the local build uses the stub. To exercise the real CloudWatch integration you need to
(1) enable the profile, (2) provide valid AWS credentials, and (3) point the dimensions at real
resources.

### 1. Enable the `use-cloudwatch` profile

Add `use-cloudwatch` to the active profiles of your Run Configuration, alongside your usual local
profiles, e.g.:

```
local-no-auth,use-cloudwatch
```

This deactivates `StubCloudWatchMetricsClient` and activates `AwsCloudWatchMetricsClient` plus the
two real CloudWatch client beans in `CloudWatchConfig`.

### 2. Provide AWS credentials

The clients resolve credentials via the standard AWS default credential chain — the same mechanism
as the existing `S3Client`. Use `aws-vault` as a credential server exactly as described in the main
[ReadMe → Connecting to AWS](../ReadMe.md#connecting-to-aws):

```shell
aws-vault exec <profile> --server
env | grep AWS_CONTAINER
```

Copy the resulting `AWS_CONTAINER_CREDENTIALS_FULL_URI` and `AWS_CONTAINER_AUTHORIZATION_TOKEN` lines
into your `.env`, and ensure `AWS_REGION=eu-west-2` is set. The role you use must have the
`cloudwatch:GetMetricStatistics` permission.

You can sanity-check your credentials before launching the app:

```shell
aws cloudwatch list-metrics --namespace "ECS/ContainerInsights" --profile <profile>
```

If that returns a `403 The security token included in the request is invalid`, the app will too —
that error is purely an authentication problem (invalid/expired/wrong-account credentials), **not**
something missing from the request.

### 3. Point the dimensions at real resources

The dimension identifiers default to **empty**, which makes CloudWatch return no datapoints (blank
rows, not an error). Set them to the real resource identifiers for the environment whose metrics you
want to see:

| Environment variable               | Maps to                                       | Example                  |
|------------------------------------|-----------------------------------------------|--------------------------|
| `CLOUDWATCH_ECS_CLUSTER_NAME`      | `cloudwatch-metrics.ecs.cluster-name`         | the ECS cluster name     |
| `CLOUDWATCH_ECS_SERVICE_NAME`      | `cloudwatch-metrics.ecs.service-name`         | the ECS service name     |
| `CLOUDWATCH_ELASTICACHE_CLUSTER_ID`| `cloudwatch-metrics.elasticache.cache-cluster-id` | the ElastiCache cluster id |
| `CLOUDFRONT_DISTRIBUTION_ID`       | `cloudwatch-metrics.cloudfront.distribution-id`   | the CloudFront distribution id |

The namespaces and metric names already default sensibly (see the table above) and only need
overriding if your environment differs. The full set of overridable variables is:

| Environment variable               | Default                  |
|------------------------------------|--------------------------|
| `CLOUDWATCH_ECS_NAMESPACE`         | `ECS/ContainerInsights`  |
| `CLOUDWATCH_ECS_MEMORY_METRIC`     | `MemoryUtilization`      |
| `CLOUDWATCH_ECS_CPU_METRIC`        | `CPUUtilization`         |
| `CLOUDWATCH_ELASTICACHE_NAMESPACE` | `AWS/ElastiCache`        |
| `CLOUDWATCH_ELASTICACHE_CPU_METRIC`| `CPUUtilization`         |
| `CLOUDWATCH_CLOUDFRONT_NAMESPACE`  | `AWS/CloudFront`         |
| `CLOUDWATCH_CLOUDFRONT_4XX_METRIC` | `4xxErrorRate`           |
| `CLOUDWATCH_CLOUDFRONT_5XX_METRIC` | `5xxErrorRate`           |

### 4. Run and submit a date range

Start the service with the profile above, open `/system-operator/metrics`, and submit a date range
**within the metrics' retention window** (e.g. the last few days). The six infrastructure rows will
show live values.

## Troubleshooting

| Symptom                                                             | Likely cause                                                                 |
|---------------------------------------------------------------------|------------------------------------------------------------------------------|
| `403 The security token included in the request is invalid`         | Invalid/expired/wrong-account AWS credentials, or missing `use-cloudwatch`-time setup. Re-run `aws-vault exec` and check `AWS_REGION`. |
| Rows show **"No data"** but no error is logged                      | Dimension identifiers are empty or wrong, or there are no datapoints in the selected date range. |
| All infrastructure rows are the same fixed stub values              | You're still on the stub — the `use-cloudwatch` profile isn't active.        |
