package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.CloudWatchMetricsClient
import uk.gov.communities.prsdb.webapp.models.dataModels.CloudWatchMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod

@PrsdbWebService
class CloudWatchMetricsService(
    private val client: CloudWatchMetricsClient,
    @Value("\${cloudwatch-metrics.ecs.namespace}") private val ecsNamespace: String,
    @Value("\${cloudwatch-metrics.ecs.memory-utilisation-metric}") private val ecsMemoryMetric: String,
    @Value("\${cloudwatch-metrics.ecs.cpu-utilisation-metric}") private val ecsCpuMetric: String,
    @Value("\${cloudwatch-metrics.ecs.cluster-name}") private val ecsClusterName: String,
    @Value("\${cloudwatch-metrics.ecs.service-name}") private val ecsServiceName: String,
    @Value("\${cloudwatch-metrics.elasticache.namespace}") private val elastiCacheNamespace: String,
    @Value("\${cloudwatch-metrics.elasticache.cpu-utilisation-metric}") private val elastiCacheCpuMetric: String,
    @Value("\${cloudwatch-metrics.elasticache.cache-cluster-id}") private val elastiCacheClusterId: String,
    @Value("\${cloudwatch-metrics.cloudfront.namespace}") private val cloudFrontNamespace: String,
    @Value("\${cloudwatch-metrics.cloudfront.client-error-rate-metric}") private val cloudFrontClientErrorRateMetric: String,
    @Value("\${cloudwatch-metrics.cloudfront.server-error-rate-metric}") private val cloudFrontServerErrorRateMetric: String,
    @Value("\${cloudwatch-metrics.cloudfront.distribution-id}") private val cloudFrontDistributionId: String,
) {
    fun getMetrics(period: ReportingPeriod): CloudWatchMetricsDataModel =
        try {
            CloudWatchMetricsDataModel(
                peakMemoryUtilisation =
                    client.getMetricStatistic(ecsNamespace, ecsMemoryMetric, ecsDimensions(), Statistic.MAXIMUM, period),
                averageMemoryUtilisation =
                    client.getMetricStatistic(ecsNamespace, ecsMemoryMetric, ecsDimensions(), Statistic.AVERAGE, period),
                peakCpuUtilisation =
                    client.getMetricStatistic(ecsNamespace, ecsCpuMetric, ecsDimensions(), Statistic.MAXIMUM, period),
                elastiCacheCpuUtilisation =
                    client.getMetricStatistic(
                        elastiCacheNamespace,
                        elastiCacheCpuMetric,
                        elastiCacheDimensions(),
                        Statistic.MAXIMUM,
                        period,
                    ),
                cloudFrontClientErrorRate =
                    client.getMetricStatistic(
                        cloudFrontNamespace,
                        cloudFrontClientErrorRateMetric,
                        cloudFrontDimensions(),
                        Statistic.AVERAGE,
                        period,
                        Region.US_EAST_1,
                    ),
                cloudFrontServerErrorRate =
                    client.getMetricStatistic(
                        cloudFrontNamespace,
                        cloudFrontServerErrorRateMetric,
                        cloudFrontDimensions(),
                        Statistic.AVERAGE,
                        period,
                        Region.US_EAST_1,
                    ),
            )
        } catch (e: Exception) {
            println("Failed to fetch CloudWatch metrics: ${e.message}")
            CloudWatchMetricsDataModel(null, null, null, null, null, null)
        }

    private fun ecsDimensions() =
        listOf(
            Dimension.builder().name("ClusterName").value(ecsClusterName).build(),
            Dimension.builder().name("ServiceName").value(ecsServiceName).build(),
        )

    private fun elastiCacheDimensions() = listOf(Dimension.builder().name("CacheClusterId").value(elastiCacheClusterId).build())

    private fun cloudFrontDimensions() =
        listOf(
            Dimension.builder().name("DistributionId").value(cloudFrontDistributionId).build(),
            Dimension.builder().name("Region").value("Global").build(),
        )
}
