package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.CloudWatchMetricsClient
import uk.gov.communities.prsdb.webapp.models.dataModels.CloudWatchMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import kotlin.math.roundToLong

@PrsdbWebService
class CloudWatchMetricsService(
    private val client: CloudWatchMetricsClient,
    @Value("\${cloudwatch-metrics.ecs.namespace}") private val ecsNamespace: String,
    @Value("\${cloudwatch-metrics.ecs.memory-utilisation-metric}") private val ecsMemoryMetric: String,
    @Value("\${cloudwatch-metrics.ecs.cluster-name}") private val ecsClusterName: String,
    @Value("\${cloudwatch-metrics.ecs.service-name}") private val ecsServiceName: String,
    @Value("\${cloudwatch-metrics.alb.namespace}") private val albNamespace: String,
    @Value("\${cloudwatch-metrics.alb.client-error-metric}") private val albClientErrorMetric: String,
    @Value("\${cloudwatch-metrics.alb.server-error-metric}") private val albServerErrorMetric: String,
    @Value("\${cloudwatch-metrics.alb.load-balancer-dimension}") private val albLoadBalancerDimension: String,
) {
    fun getMetrics(period: ReportingPeriod): CloudWatchMetricsDataModel =
        try {
            CloudWatchMetricsDataModel(
                peakMemoryUtilisation =
                    client.getMetricStatistic(ecsNamespace, ecsMemoryMetric, ecsDimensions(), Statistic.MAXIMUM, period),
                averageMemoryUtilisation =
                    client.getMetricStatistic(ecsNamespace, ecsMemoryMetric, ecsDimensions(), Statistic.AVERAGE, period),
                albClientErrorCount =
                    client
                        .getMetricStatistic(albNamespace, albClientErrorMetric, albDimensions(), Statistic.SUM, period)
                        ?.roundToLong(),
                albServerErrorCount =
                    client
                        .getMetricStatistic(albNamespace, albServerErrorMetric, albDimensions(), Statistic.SUM, period)
                        ?.roundToLong(),
            )
        } catch (e: Exception) {
            println("Failed to fetch CloudWatch metrics: ${e.message}")
            CloudWatchMetricsDataModel(null, null, null, null)
        }

    private fun ecsDimensions() =
        listOf(
            Dimension.builder().name("ClusterName").value(ecsClusterName).build(),
            Dimension.builder().name("ServiceName").value(ecsServiceName).build(),
        )

    private fun albDimensions() = listOf(Dimension.builder().name("LoadBalancer").value(albLoadBalancerDimension).build())
}
