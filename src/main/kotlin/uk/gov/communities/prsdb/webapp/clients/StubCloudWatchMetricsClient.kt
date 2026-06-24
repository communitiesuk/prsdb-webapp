package uk.gov.communities.prsdb.webapp.clients

import org.springframework.context.annotation.Profile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod

@Profile("local & !use-cloudwatch")
@PrsdbWebService
class StubCloudWatchMetricsClient : CloudWatchMetricsClient {
    override fun getMetricStatistic(
        namespace: String,
        metricName: String,
        dimensions: List<Dimension>,
        statistic: Statistic,
        period: ReportingPeriod,
        region: Region?,
    ): Double =
        when {
            namespace.contains("ElastiCache", ignoreCase = true) -> STUB_ELASTICACHE_CPU_UTILISATION
            namespace.contains("CloudFront", ignoreCase = true) && metricName.startsWith("4xx") -> STUB_CLIENT_ERROR_RATE
            namespace.contains("CloudFront", ignoreCase = true) -> STUB_SERVER_ERROR_RATE
            metricName.contains("Cpu", ignoreCase = true) -> STUB_PEAK_CPU_UTILISATION
            statistic == Statistic.MAXIMUM -> STUB_PEAK_MEMORY_UTILISATION
            statistic == Statistic.AVERAGE -> STUB_AVERAGE_MEMORY_UTILISATION
            else -> 0.0
        }

    companion object {
        private const val STUB_PEAK_MEMORY_UTILISATION = 73.4
        private const val STUB_AVERAGE_MEMORY_UTILISATION = 41.2
        private const val STUB_PEAK_CPU_UTILISATION = 62.5
        private const val STUB_ELASTICACHE_CPU_UTILISATION = 18.9
        private const val STUB_CLIENT_ERROR_RATE = 0.82
        private const val STUB_SERVER_ERROR_RATE = 0.05
    }
}
