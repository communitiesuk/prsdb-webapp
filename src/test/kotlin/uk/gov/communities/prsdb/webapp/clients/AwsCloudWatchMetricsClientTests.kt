package uk.gov.communities.prsdb.webapp.clients

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Datapoint
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Instant
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class AwsCloudWatchMetricsClientTests {
    @Mock
    private lateinit var sdkClient: CloudWatchClient

    private val period =
        ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-20T23:59:59Z"))

    private fun client() = AwsCloudWatchMetricsClient(sdkClient)

    private fun stubResponse(response: GetMetricStatisticsResponse) {
        whenever(sdkClient.getMetricStatistics(any<Consumer<GetMetricStatisticsRequest.Builder>>()))
            .thenReturn(response)
    }

    @Test
    fun `getMetricStatistic returns the maximum across returned datapoints for MAXIMUM`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(
                    Datapoint.builder().maximum(40.0).build(),
                    Datapoint.builder().maximum(72.5).build(),
                ).build(),
        )

        val result = client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.MAXIMUM, period)

        assertEquals(72.5, result)
    }

    @Test
    fun `getMetricStatistic returns the mean of datapoint averages for AVERAGE`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(
                    Datapoint.builder().average(40.0).build(),
                    Datapoint.builder().average(60.0).build(),
                ).build(),
        )

        val result = client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.AVERAGE, period)

        assertEquals(50.0, result)
    }

    @Test
    fun `getMetricStatistic sums datapoint sums for SUM`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(
                    Datapoint.builder().sum(100.0).build(),
                    Datapoint.builder().sum(28.0).build(),
                ).build(),
        )

        val result = client().getMetricStatistic("ns", "Errors", emptyList(), Statistic.SUM, period)

        assertEquals(128.0, result)
    }

    @Test
    fun `getMetricStatistic returns null when there are no datapoints`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())

        assertNull(client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.MAXIMUM, period))
    }

    @Test
    fun `getMetricStatistic returns null for an unsupported statistic`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(Datapoint.builder().minimum(10.0).build())
                .build(),
        )

        assertNull(client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.MINIMUM, period))
    }

    @Test
    fun `getMetricStatistic builds the request with the supplied identifiers, period bounds and granularity`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())
        val dimensions = listOf(Dimension.builder().name("ClusterName").value("prod").build())

        client().getMetricStatistic("AWS/ECS", "MemoryUtilization", dimensions, Statistic.MAXIMUM, period)

        val captor = argumentCaptor<Consumer<GetMetricStatisticsRequest.Builder>>()
        verify(sdkClient).getMetricStatistics(captor.capture())
        val builder = GetMetricStatisticsRequest.builder()
        captor.firstValue.accept(builder)
        val request = builder.build()

        assertEquals("AWS/ECS", request.namespace())
        assertEquals("MemoryUtilization", request.metricName())
        assertEquals(dimensions, request.dimensions())
        assertEquals(period.start, request.startTime())
        assertEquals(period.end, request.endTime())
        assertEquals(listOf(Statistic.MAXIMUM), request.statistics())
        // 10-day, ~24h-short range / 60 divisions, floored to whole minutes -> 15780s
        assertEquals(15780, request.period())
    }

    @Test
    fun `getMetricStatistic floors the granularity to a minimum of 60 seconds for a short range`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())
        val shortPeriod =
            ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-10T00:30:00Z"))

        client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.MAXIMUM, shortPeriod)

        val captor = argumentCaptor<Consumer<GetMetricStatisticsRequest.Builder>>()
        verify(sdkClient).getMetricStatistics(captor.capture())
        val builder = GetMetricStatisticsRequest.builder()
        captor.firstValue.accept(builder)

        assertEquals(60, builder.build().period())
    }
}
