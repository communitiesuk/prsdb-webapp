package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.clients.CloudWatchMetricsClient
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class CloudWatchMetricsServiceTests {
    @Mock
    private lateinit var client: CloudWatchMetricsClient

    private val period =
        ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-20T23:59:59Z"))

    private fun service() =
        CloudWatchMetricsService(
            client,
            ecsNamespace = "AWS/ECS",
            ecsMemoryMetric = "MemoryUtilization",
            ecsCpuMetric = "CPUUtilization",
            ecsClusterName = "cluster",
            ecsServiceName = "service",
            elastiCacheNamespace = "AWS/ElastiCache",
            elastiCacheCpuMetric = "CPUUtilization",
            elastiCacheClusterId = "cache-cluster",
            cloudFrontNamespace = "AWS/CloudFront",
            cloudFrontClientErrorRateMetric = "4xxErrorRate",
            cloudFrontServerErrorRateMetric = "5xxErrorRate",
            cloudFrontDistributionId = "distribution",
        )

    @Test
    fun `getMetrics maps memory, cpu, elasticache and cloudfront error rate values`() {
        whenever(
            client.getMetricStatistic(
                eq("AWS/ECS"),
                eq("MemoryUtilization"),
                any(),
                eq(Statistic.MAXIMUM),
                any(),
                anyOrNull(),
            ),
        ).thenReturn(73.42)
        whenever(
            client.getMetricStatistic(
                eq("AWS/ECS"),
                eq("MemoryUtilization"),
                any(),
                eq(Statistic.AVERAGE),
                any(),
                anyOrNull(),
            ),
        ).thenReturn(41.21)
        whenever(
            client.getMetricStatistic(eq("AWS/ECS"), eq("CPUUtilization"), any(), eq(Statistic.MAXIMUM), any(), anyOrNull()),
        ).thenReturn(62.5)
        whenever(
            client.getMetricStatistic(eq("AWS/ElastiCache"), eq("CPUUtilization"), any(), eq(Statistic.MAXIMUM), any(), anyOrNull()),
        ).thenReturn(18.9)
        whenever(
            client.getMetricStatistic(eq("AWS/CloudFront"), eq("4xxErrorRate"), any(), eq(Statistic.AVERAGE), any(), eq(Region.US_EAST_1)),
        ).thenReturn(0.82)
        whenever(
            client.getMetricStatistic(eq("AWS/CloudFront"), eq("5xxErrorRate"), any(), eq(Statistic.AVERAGE), any(), eq(Region.US_EAST_1)),
        ).thenReturn(0.05)

        val result = service().getMetrics(period)

        assertEquals(73.42, result.peakMemoryUtilisation)
        assertEquals(41.21, result.averageMemoryUtilisation)
        assertEquals(62.5, result.peakCpuUtilisation)
        assertEquals(18.9, result.elastiCacheCpuUtilisation)
        assertEquals(0.82, result.cloudFrontClientErrorRate)
        assertEquals(0.05, result.cloudFrontServerErrorRate)
    }

    @Test
    fun `getMetrics returns null values when the client returns no data`() {
        whenever(client.getMetricStatistic(any(), any(), any(), any(), any(), anyOrNull())).thenReturn(null)

        val result = service().getMetrics(period)

        assertNull(result.peakMemoryUtilisation)
        assertNull(result.averageMemoryUtilisation)
        assertNull(result.peakCpuUtilisation)
        assertNull(result.elastiCacheCpuUtilisation)
        assertNull(result.cloudFrontClientErrorRate)
        assertNull(result.cloudFrontServerErrorRate)
    }

    @Test
    fun `getMetrics returns all nulls when the client throws`() {
        whenever(client.getMetricStatistic(any(), any(), any(), any(), any(), anyOrNull())).thenThrow(RuntimeException("boom"))

        val result = service().getMetrics(period)

        assertNull(result.peakMemoryUtilisation)
        assertNull(result.averageMemoryUtilisation)
        assertNull(result.peakCpuUtilisation)
        assertNull(result.elastiCacheCpuUtilisation)
        assertNull(result.cloudFrontClientErrorRate)
        assertNull(result.cloudFrontServerErrorRate)
    }
}
