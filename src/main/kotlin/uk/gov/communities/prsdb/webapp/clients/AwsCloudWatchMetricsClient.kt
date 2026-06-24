package uk.gov.communities.prsdb.webapp.clients

import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import kotlin.math.max

@Profile("!local")
@PrsdbWebService
class AwsCloudWatchMetricsClient(
    private val sdkClient: CloudWatchClient,
) : CloudWatchMetricsClient {
    override fun getMetricStatistic(
        namespace: String,
        metricName: String,
        dimensions: List<Dimension>,
        statistic: Statistic,
        period: ReportingPeriod,
    ): Double? {
        val response =
            sdkClient.getMetricStatistics { request ->
                request
                    .namespace(namespace)
                    .metricName(metricName)
                    .dimensions(dimensions)
                    .startTime(period.start)
                    .endTime(period.end)
                    .period(periodSeconds(period))
                    .statistics(statistic)
            }
        val datapoints = response.datapoints()
        if (datapoints.isEmpty()) return null
        return when (statistic) {
            Statistic.MAXIMUM -> datapoints.mapNotNull { it.maximum() }.maxOrNull()
            Statistic.AVERAGE -> datapoints.mapNotNull { it.average() }.average().takeIf { it.isFinite() }
            Statistic.SUM -> datapoints.mapNotNull { it.sum() }.sum()
            else -> null
        }
    }

    private fun periodSeconds(period: ReportingPeriod): Int {
        val rangeSeconds = period.end.epochSecond - period.start.epochSecond
        val granularity = ((rangeSeconds / GRANULARITY_DIVISIONS) / SECONDS_PER_MINUTE) * SECONDS_PER_MINUTE
        return max(granularity, MIN_PERIOD_SECONDS).toInt()
    }

    companion object {
        private const val GRANULARITY_DIVISIONS = 60L
        private const val SECONDS_PER_MINUTE = 60L
        private const val MIN_PERIOD_SECONDS = 60L
    }
}
