package uk.gov.communities.prsdb.webapp.clients

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod

interface CloudWatchMetricsClient {
    fun getMetricStatistic(
        namespace: String,
        metricName: String,
        dimensions: List<Dimension>,
        statistic: Statistic,
        period: ReportingPeriod,
        region: Region? = null,
    ): Double?
}
