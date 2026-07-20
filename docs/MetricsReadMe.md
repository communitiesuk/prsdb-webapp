# Metrics

The system operator metrics page (`/system-operator/metrics`) shows a single summary list combining:

- **Service usage metrics** — landlord/property registration counts and registration-to-first-property-association
  percentiles, from the database (`MetricsService`).
- **Journey completion rates** — from the Plausible Stats API (`PlausibleMetricsService`, see
  [AnalyticsReadMe](AnalyticsReadMe.md)).
- **Transaction counts** — completed transactions from the Plausible `Transaction` custom event
  (`PlausibleMetricsService`), described below.
- **Infrastructure metrics** — from Amazon CloudWatch (`CloudWatchMetricsService`), described below.

The page (`MetricsController`) shows no rows until the operator submits a **reporting period** (From/To
dates); on submit, the four services are queried for that period and their results are rendered as one
combined summary list (`getMetricRows`). Counts are formatted as integers, durations via
`MetricsDurationHelper`, and rates/utilisations as `0.00%`. Any value that is missing — or whose upstream
call failed — renders as **"No data"** rather than erroring the page. Row labels come from
`messages/metrics.yml` (`metrics.rows.*`).

## Dashboard rows

The summary list contains the following rows, in display order:

| # | Dashboard label | Source | Derivation |
|---|-----------------|--------|------------|
| 1 | Number of registrations (landlords) | Database (`MetricsService`) | Landlords created in the period. |
| 2 | Landlords verified by One Login | Database (`MetricsService`) | Landlords created in the period with `isVerified = true`. |
| 3 | Number of properties | Database (`MetricsService`) | Property ownerships created in the period. |
| 4 | Number of landlords with at least 1 property | Database (`MetricsService`) | Distinct landlords whose ownership link was created in the period. |
| 5 | Median time between registration and first property registration or joining an existing property | Database (`MetricsService`) | Median (p50) of registration→earliest-ownership-link durations. |
| 6 | 90th percentile time between registration and first property registration or joining an existing property | Database (`MetricsService`) | p90 of the same durations. |
| 7 | 95th percentile time between registration and first property registration or joining an existing property | Database (`MetricsService`) | p95 of the same durations. |
| 8 | Landlord registration completion rate | Plausible Stats API (`PlausibleMetricsService.getCompletionRates`) | Unique **visitors** at the confirmation page ÷ start page. |
| 9 | Property registration completion rate | Plausible Stats API (`PlausibleMetricsService.getCompletionRates`) | **Page views** at confirmation ÷ start (a landlord may register several properties). |
| 10 | Local council user registration completion rate | Plausible Stats API (`PlausibleMetricsService.getCompletionRates`) | Unique **visitors** at confirmation ÷ privacy-notice page. |
| 11 | Peak memory utilisation | CloudWatch (`CloudWatchMetricsService`) | See [CloudWatch infrastructure metrics](#cloudwatch-infrastructure-metrics). |
| 12 | Average memory utilisation | CloudWatch (`CloudWatchMetricsService`) | See below. |
| 13 | Peak CPU utilisation | CloudWatch (`CloudWatchMetricsService`) | See below. |
| 14 | ElastiCache CPU utilisation | CloudWatch (`CloudWatchMetricsService`) | See below. |
| 15 | Client error rate (HTTP 4xx) | CloudWatch (`CloudWatchMetricsService`) | See below. |
| 16 | Server error rate (HTTP 5xx) | CloudWatch (`CloudWatchMetricsService`) | See below. |
| 17 | Total number of transactions | Plausible (`PlausibleMetricsService.getTransactionCounts`) | See [Transaction counts](#transaction-counts). |

> **Completion rates** use visitors for landlord and local council user registration but page views for
> property registration, because a single landlord may register multiple properties. This is surfaced on
> the page via `metrics.completionRateExplanation`.

## Transaction counts

Completed transactions — registrations, deregistrations, updates, switch-to-individual, and accepting a
joint landlord invitation — are counted from a dedicated Plausible `Transaction` custom event. The event
is fired by a button press on each journey's final commit step: the commit button is rendered from a
fragment tagged `data-plausible-event="Transaction"` (`transactionSubmitButton` / `transactionWarningButton`).
For reporting periods before the configured cutover date (`plausible.transaction-event-start-date`) the
legacy Flow event is used instead. See [AnalyticsReadMe](AnalyticsReadMe.md) and `PlausibleMetricsService`
for details.

### Known coverage gap: landlord deregistration with no registered properties

A landlord who has **no registered properties** deregisters via the `are-you-sure` page, which goes
straight to the internal deregistration step and skips the `reason` page that carries the `Transaction`
tag. There is no clean place to fire the event for this path: the `are-you-sure` page is a yes/no
question (so its button would fire the event even when the user chooses *not* to proceed), and the
deregistration step itself has no button to tag.

**Decision:** these deregistrations are intentionally **not counted**. The volume is expected to be
minimal (a landlord with no properties leaving the service) and there is no clean solution that avoids
over-counting. Landlord deregistrations where the landlord *does* have properties, and all property
deregistrations, are counted as normal.

## CloudWatch infrastructure metrics

`CloudWatchMetricsService` fetches the following six metrics for the selected date range via
`CloudWatchMetricsClient.getMetricStatistic`:

| Dashboard row                 | Namespace             | Metric             | Statistic | Dimensions                              | Region      |
|-------------------------------|-----------------------|--------------------|-----------|-----------------------------------------|-------------|
| Peak memory utilisation       | `AWS/ECS`               | `MemoryUtilization` | Maximum   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| Average memory utilisation    | `AWS/ECS`               | `MemoryUtilization` | Average   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| Peak CPU utilisation          | `AWS/ECS`               | `CPUUtilization`    | Maximum   | `ClusterName`, `ServiceName`            | `eu-west-2` |
| ElastiCache CPU utilisation   | `AWS/ElastiCache`       | `CPUUtilization`    | Maximum   | `CacheClusterId`                        | `eu-west-2` |
| Client error rate (HTTP 4xx)  | `AWS/CloudFront`        | `4xxErrorRate`      | Average   | `DistributionId`, `Region=Global`       | `us-east-1` |
| Server error rate (HTTP 5xx)  | `AWS/CloudFront`        | `5xxErrorRate`      | Average   | `DistributionId`, `Region=Global`       | `us-east-1` |

> **CloudFront is special.** CloudFront only publishes its metrics to **`us-east-1`** with a
> `Region=Global` dimension, so they are queried through a separate `us-east-1` CloudWatch client
> (`cloudFrontCloudWatchClient` in `CloudWatchConfig`). All other metrics use the application's
> configured region (`eu-west-2`).

### Average statistics are volume-weighted

For `Average` metrics (the CloudFront 4xx/5xx error rates and average memory utilisation) the client
does **not** take a plain mean of the per-bucket averages — that "average of averages" is wrong for a
rate and is sensitive to how CloudWatch buckets the data. Instead it requests `SampleCount` alongside
`Average` and returns the volume-weighted mean:

```
Σ (average × sampleCount) / Σ sampleCount
```

This matches the AWS console's single-period `Average` and is invariant to the bucket size. It returns
**"No data"** when the total sample count is zero (or the result is non-finite).

### The query period is age-aware

CloudWatch retains 1-minute data for 15 days and 5-minute data for 63 days, and a requested `period`
must be a multiple of the finest resolution still available for the range. The client rounds the
bucket size (the range divided into ~60 buckets) **up** to a valid resolution based on the age of the
range start relative to now:

| Age of range start | Resolution used |
|--------------------|-----------------|
| ≤ 15 days          | 60s             |
| ≤ 63 days          | 300s            |
| > 63 days          | 3600s           |

with a 60-second floor. Without this, a fixed historic range would **drift** between refreshes: as
wall-clock time advances the range aged past a retention boundary, CloudWatch re-served it from a
coarser rollup, changing the datapoint set and — via the old average-of-averages — the displayed 2dp
value. The weighted average above and this age-aware period together keep an aged fixed range stable.

If a metric returns no data (or its AWS call fails) the service logs
`Failed to fetch CloudWatch metrics: ...` and **only that row** renders **"No data"** — each of the six
metrics is fetched in isolation, so one failing metric no longer blanks the others.

## Client selection by profile

There are two implementations of `CloudWatchMetricsClient`, selected by Spring profile so that
exactly one bean is active at a time:

| Implementation                 | Profile expression | Behaviour                                          |
|--------------------------------|--------------------|----------------------------------------------------|
| `StubCloudWatchMetricsClient`  | `local`            | Returns fixed stub values — **the local default**. |
| `AwsCloudWatchMetricsClient`   | `!local`           | Calls real CloudWatch via the AWS SDK.             |

So when you run locally you always get the stub, and **no AWS credentials are required**. Real
CloudWatch is only called in deployed (non-`local`) environments.
