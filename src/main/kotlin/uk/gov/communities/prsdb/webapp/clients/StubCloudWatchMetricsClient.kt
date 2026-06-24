package uk.gov.communities.prsdb.webapp.clients

import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod

@Profile("local")
@PrsdbWebService
class StubCloudWatchMetricsClient : CloudWatchMetricsClient {
    override fun getMetricStatistic(
        namespace: String,
        metricName: String,
        dimensions: List<Dimension>,
        statistic: Statistic,
        period: ReportingPeriod,
    ): Double =
        when (statistic) {
            Statistic.MAXIMUM -> STUB_PEAK_MEMORY_UTILISATION
            Statistic.AVERAGE -> STUB_AVERAGE_MEMORY_UTILISATION
            Statistic.SUM -> STUB_ERROR_COUNT
            else -> 0.0
        }

    companion object {
        private const val STUB_PEAK_MEMORY_UTILISATION = 73.4
        private const val STUB_AVERAGE_MEMORY_UTILISATION = 41.2
        private const val STUB_ERROR_COUNT = 128.0
    }
}
