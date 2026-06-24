package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
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
            ecsNamespace = "ECS/ContainerInsights",
            ecsMemoryMetric = "MemoryUtilization",
            ecsClusterName = "cluster",
            ecsServiceName = "service",
            albNamespace = "AWS/ApplicationELB",
            albClientErrorMetric = "HTTPCode_Target_4XX_Count",
            albServerErrorMetric = "HTTPCode_Target_5XX_Count",
            albLoadBalancerDimension = "app/lb/123",
        )

    @Test
    fun `getMetrics maps peak and average memory and rounds error sums to longs`() {
        whenever(client.getMetricStatistic(any(), eq("MemoryUtilization"), any(), eq(Statistic.MAXIMUM), any()))
            .thenReturn(73.42)
        whenever(client.getMetricStatistic(any(), eq("MemoryUtilization"), any(), eq(Statistic.AVERAGE), any()))
            .thenReturn(41.21)
        whenever(client.getMetricStatistic(any(), eq("HTTPCode_Target_4XX_Count"), any(), eq(Statistic.SUM), any()))
            .thenReturn(128.0)
        whenever(client.getMetricStatistic(any(), eq("HTTPCode_Target_5XX_Count"), any(), eq(Statistic.SUM), any()))
            .thenReturn(3.0)

        val result = service().getMetrics(period)

        assertEquals(73.42, result.peakMemoryUtilisation)
        assertEquals(41.21, result.averageMemoryUtilisation)
        assertEquals(128L, result.albClientErrorCount)
        assertEquals(3L, result.albServerErrorCount)
    }

    @Test
    fun `getMetrics returns null memory and error values when the client returns no data`() {
        whenever(client.getMetricStatistic(any(), any(), any(), any(), any())).thenReturn(null)

        val result = service().getMetrics(period)

        assertNull(result.peakMemoryUtilisation)
        assertNull(result.averageMemoryUtilisation)
        assertNull(result.albClientErrorCount)
        assertNull(result.albServerErrorCount)
    }

    @Test
    fun `getMetrics returns all nulls when the client throws`() {
        whenever(client.getMetricStatistic(any(), any(), any(), any(), any())).thenThrow(RuntimeException("boom"))

        val result = service().getMetrics(period)

        assertNull(result.peakMemoryUtilisation)
        assertNull(result.averageMemoryUtilisation)
        assertNull(result.albClientErrorCount)
        assertNull(result.albServerErrorCount)
    }
}
