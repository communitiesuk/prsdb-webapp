package uk.gov.communities.prsdb.webapp.clients

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Datapoint
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max

@Profile("!local")
@PrsdbWebService
class AwsCloudWatchMetricsClient(
    private val sdkClient: CloudWatchClient,
    @Qualifier("cloudFrontCloudWatchClient") private val cloudFrontSdkClient: CloudWatchClient,
    private val clock: Clock = Clock.systemUTC(),
) : CloudWatchMetricsClient {
    override fun getMetricStatistic(
        namespace: String,
        metricName: String,
        dimensions: List<Dimension>,
        statistic: Statistic,
        period: ReportingPeriod,
        region: Region?,
    ): Double? {
        val client = if (region == Region.US_EAST_1) cloudFrontSdkClient else sdkClient
        val response =
            client.getMetricStatistics { request ->
                request
                    .namespace(namespace)
                    .metricName(metricName)
                    .dimensions(dimensions)
                    .startTime(period.start)
                    .endTime(period.end)
                    .period(periodSeconds(period))
                    .statistics(requestedStatistics(statistic))
            }
        val datapoints = response.datapoints()
        if (datapoints.isEmpty()) return null
        return when (statistic) {
            Statistic.MAXIMUM -> datapoints.mapNotNull { it.maximum() }.maxOrNull()
            Statistic.AVERAGE -> volumeWeightedAverage(datapoints)
            Statistic.SUM -> datapoints.mapNotNull { it.sum() }.sum()
            else -> null
        }
    }

    private fun requestedStatistics(statistic: Statistic): List<Statistic> =
        if (statistic == Statistic.AVERAGE) {
            listOf(Statistic.AVERAGE, Statistic.SAMPLE_COUNT)
        } else {
            listOf(statistic)
        }

    private fun volumeWeightedAverage(datapoints: List<Datapoint>): Double? {
        var weightedSum = 0.0
        var totalSamples = 0.0
        for (datapoint in datapoints) {
            val average = datapoint.average() ?: continue
            val sampleCount = datapoint.sampleCount() ?: continue
            weightedSum += average * sampleCount
            totalSamples += sampleCount
        }
        if (totalSamples == 0.0) return null
        return (weightedSum / totalSamples).takeIf { it.isFinite() }
    }

    private fun periodSeconds(period: ReportingPeriod): Int {
        val rangeSeconds = period.end.epochSecond - period.start.epochSecond
        val rawBucketSeconds = rangeSeconds / GRANULARITY_DIVISIONS
        val resolution = validResolutionSeconds(period.start)
        val roundedUp = ceil(rawBucketSeconds.toDouble() / resolution).toLong() * resolution
        return max(roundedUp, MIN_PERIOD_SECONDS).toInt()
    }

    private fun validResolutionSeconds(start: Instant): Long {
        val ageDays = ChronoUnit.DAYS.between(start, clock.instant())
        return when {
            ageDays <= HIGH_RES_MAX_AGE_DAYS -> HIGH_RES_SECONDS
            ageDays <= MEDIUM_RES_MAX_AGE_DAYS -> MEDIUM_RES_SECONDS
            else -> LOW_RES_SECONDS
        }
    }

    companion object {
        private const val GRANULARITY_DIVISIONS = 60L
        private const val MIN_PERIOD_SECONDS = 60L
        private const val HIGH_RES_SECONDS = 60L
        private const val MEDIUM_RES_SECONDS = 300L
        private const val LOW_RES_SECONDS = 3600L
        private const val HIGH_RES_MAX_AGE_DAYS = 15L
        private const val MEDIUM_RES_MAX_AGE_DAYS = 63L
    }
}
