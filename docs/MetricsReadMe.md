# Metrics

The system operator metrics page (`/system-operator/metrics`) shows a single summary list combining:

- **Service usage metrics** — landlord/property registration counts and registration-to-first-property-association
  percentiles, from the database (`MetricsService`).
- **Journey completion rates** — from the Plausible Stats API (`PlausibleMetricsService`, see
  [AnalyticsReadMe](AnalyticsReadMe.md)).
- **Transaction counts** — completed transactions from the Plausible `Transaction` custom event
  (`PlausibleMetricsService`), described below.
- **Cost metrics** — account-wide AWS costs from Cost Explorer (`CostExplorerMetricsService`), described
  below. Cost per transaction combines the Cost Explorer cost with the Plausible transaction count.
- **Infrastructure metrics** — from Amazon CloudWatch (`CloudWatchMetricsService`), described below.

The page (`MetricsController`) shows no rows until the operator submits a **reporting period** (From/To
dates); on submit, five metric queries are run for that period and their results are rendered as one
combined summary list (`getMetricRows`). Counts are formatted as integers, durations via
`MetricsDurationHelper`, and rates/utilisations as `0.00%`. Any value that is missing — or whose upstream
call failed — renders as **"No data"** rather than erroring the page. Row labels come from
`messages/metrics.yml` (`metrics.rows.*`).

## Dashboard rows

The summary list contains 19 rows, in display order:

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
| 18 | Total AWS cost | Cost Explorer (`CostExplorerMetricsService`) | Account-wide daily `UnblendedCost` summed for the selected period. |
| 19 | Cost per transaction | Cost Explorer and Plausible (`CostExplorerMetricsService`, `PlausibleMetricsService.getTransactionCounts`) | Total AWS cost ÷ total transactions; **No data** when the transaction count is zero. |

> **Completion rates** use visitors for landlord and local council user registration but page views for
> property registration, because a single landlord may register multiple properties. This is surfaced on
> the page via `metrics.completionRateExplanation`.

## Transaction counts

Completed transactions — registrations, deregistrations, updates, switch-to-individual, accepting a
joint landlord invitation, and leaving a property as a joint landlord — are counted from a dedicated
Plausible `Transaction` custom event. The event
is fired by a button press on each journey's final commit step: the commit button is rendered from a
fragment tagged `data-plausible-event="Transaction"` (`transactionSubmitButton` / `transactionWarningButton`).
For reporting periods before the configured cutover date (`plausible.transaction-event-start-date`) the
legacy Flow event is used instead. See [AnalyticsReadMe](AnalyticsReadMe.md) and `PlausibleMetricsService`
for details.

### Known coverage gaps

Some journeys, or paths through them, are intentionally not counted. In each case there is no place to
fire the event exactly once per completion, and the project's standing preference is to **under-count
rather than over-count**.

#### Landlord deregistration with no registered properties

A landlord who has **no registered properties** deregisters via the `are-you-sure` page, which goes
straight to the internal deregistration step and skips the `reason` page that carries the `Transaction`
tag. There is no clean place to fire the event for this path: the `are-you-sure` page is a yes/no
question (so its button would fire the event even when the user chooses *not* to proceed), and the
deregistration step itself has no button to tag.

**Decision:** these deregistrations are intentionally **not counted**. The volume is expected to be
minimal (a landlord with no properties leaving the service) and there is no clean solution that avoids
over-counting. Landlord deregistrations where the landlord *does* have properties, and all property
deregistrations, are counted as normal.

#### Cancelling a joint landlord invitation

The `cancelJointLandlordInvitation` journey's only button is on its `are-you-sure` page, and the
following `cancelInvitationStep` is internal with no button.

**Decision:** cancelled invitations are intentionally **not counted**, for the same reason as above —
there is no place to fire the event that does not also fire it when the user answers "no".

#### Removing a letting agent or property manager

The `cancelLettingAgentDelegation` journey's only button is on its `are-you-sure` page, and the
following `removeDelegationStep` is internal with no button.

**Decision:** removals are intentionally **not counted**, for the same reason as above. Note that the
corresponding *delegation* journey (`allowLettingAgentForm`) **is** counted, so delegations will
appear in the metrics while removals will not.

#### Landlord address updates

The landlord address update journey commits on either `select-address` (a looked-up address was
chosen) or `manual-address` (reached by choosing "Add address manually" on the select-address page,
or when the lookup found no addresses). Tagging a step's button is a render-time decision, taken
before the user's radio selection is known, so tagging both steps would fire `Transaction` twice for
a single update whenever the user reaches `manual-address` *via* `select-address`.

**Decision:** address updates are intentionally **not counted**. Tagging only `manual-address` would
count an arbitrary subset (missing the common "pick an address from the list" route), and suppressing
the event with JavaScript keyed on the selected radio would introduce a new tagging mechanism for a
single path. Under-counting was preferred to double-counting. The other four landlord detail updates
(name, email, phone number and date of birth) are counted as normal.

Note that `forms/selectAddressForm.html` and `forms/manualAddressForm.html` are shared with the
landlord registration, property registration, governing body member address and trustee address
journeys, so any future fix must tag the update journey without affecting those.

#### Occupancy updates on a property delegated to a letting agent

Where a property is currently occupied *and* delegated to a letting agent, the occupancy update
journey shows an "are you sure" interruption before saving, because making the property unoccupied
removes the letting agent from the registration. The interruption is the commit step on that path, so
it carries the `Transaction` tag and the tag is dropped from the preceding `occupied` question page —
tagging both would count the journey twice.

**Decision:** on that page, answering "yes" (leaving the property occupied) is intentionally **not
counted**. Whether the interruption follows is a render-time decision taken before the user's radio
selection is known, so the `occupied` page cannot be tagged only for the "yes" answer. The gap is
harmless in practice: the interruption is only shown when the property was *already* occupied, so
answering "yes" leaves the occupancy unchanged. All other occupancy updates — including every update
to a property with no letting agent — are counted as normal.

## Cost and cost per transaction

The deployed `AwsCostExplorerMetricsClient` calls Cost Explorer's `GetCostAndUsage` operation in
`us-east-1`. Its query is account-wide: it has no tag, service, or resource filter and no grouping. It
uses `DAILY` granularity and the `UnblendedCost` metric.

The request intentionally omits `BillingViewArn`. Cost Explorer therefore returns the costs available
to the hosting AWS account, which is the dashboard's required scope.

The dashboard reporting range is inclusive in UK time. Cost Explorer's end date is exclusive, so the
client sends the selected end date plus one day. It follows all pagination tokens, sums the returned daily
cost strings with `BigDecimal`, and returns one consistent currency unit. Valid zero and negative costs
are preserved.

Costs are displayed to two decimal places as `amount CURRENCY`. If any returned daily result is estimated,
both the total-cost and cost-per-transaction rows are marked `(estimated)`. A cost-source failure or an
invalid response makes both rows **"No data"** without hiding the other metrics. When the transaction
count is zero, the total AWS cost still displays, while cost per transaction is **"No data"**.

### Operational prerequisites

Cost Explorer must be enabled manually in AWS Billing and Cost Management; it cannot be enabled by API.
Current-month data usually takes about 24 hours to appear and refreshes at least daily. Recent and
current-month values may remain estimated until AWS completes billing reconciliation after month end. A
management account can also restrict a member account's access.

Live integration testing showed that, when `BillingViewArn` is omitted, AWS authorizes
`ce:GetCostAndUsage` against `arn:aws:ce:us-east-1:<account-id>:/GetCostAndUsage`. Infrastructure PR
[`communitiesuk/prsdb-infra#309`](https://github.com/communitiesuk/prsdb-infra/pull/309) must therefore
grant only the `ce:GetCostAndUsage` action with `Resource = "*"`. The wildcard resource applies only to
that action; it does not grant any other Cost Explorer or billing actions. The application does not
enumerate or inspect billing views and does not use the billing console, so it does not require
`billing:ListBillingViews`, `billing:GetBillingView`, or legacy `aws-portal:ViewBilling`.

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

There are also two implementations of `CostExplorerMetricsClient`, selected by Spring profile so that
exactly one bean is active at a time:

| Implementation                   | Profile expression | Behaviour                                                   |
|----------------------------------|--------------------|-------------------------------------------------------------|
| `StubCostExplorerMetricsClient`  | `local`            | Returns `123.45 USD` marked as estimated.                   |
| `AwsCostExplorerMetricsClient`   | `!local`           | Calls Cost Explorer through the AWS SDK.                     |

The local stub does not confirm that Cost Explorer is enabled or populated in any environment; the AWS
client is used only in deployed (non-`local`) environments.
