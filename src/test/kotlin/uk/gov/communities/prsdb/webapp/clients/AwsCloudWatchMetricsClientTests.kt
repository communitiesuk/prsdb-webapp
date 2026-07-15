package uk.gov.communities.prsdb.webapp.clients

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Datapoint
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse
import software.amazon.awssdk.services.cloudwatch.model.Statistic
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class AwsCloudWatchMetricsClientTests {
    @Mock
    private lateinit var sdkClient: CloudWatchClient

    @Mock
    private lateinit var cloudFrontSdkClient: CloudWatchClient

    private val period =
        ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-20T23:59:59Z"))

    private val fixedClock = Clock.fixed(Instant.parse("2025-01-21T00:00:00Z"), ZoneOffset.UTC)

    private fun client(clock: Clock = fixedClock) = AwsCloudWatchMetricsClient(sdkClient, cloudFrontSdkClient, clock)

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
    fun `getMetricStatistic returns the volume-weighted mean of datapoint averages for AVERAGE`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(
                    Datapoint.builder().average(40.0).sampleCount(1.0).build(),
                    Datapoint.builder().average(60.0).sampleCount(3.0).build(),
                ).build(),
        )

        val result = client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.AVERAGE, period)

        // (40*1 + 60*3) / (1 + 3) = 220 / 4 = 55.0
        assertEquals(55.0, result)
    }

    @Test
    fun `getMetricStatistic returns null for AVERAGE when the total sample count is zero`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(Datapoint.builder().average(40.0).sampleCount(0.0).build())
                .build(),
        )

        assertNull(client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.AVERAGE, period))
    }

    @Test
    fun `getMetricStatistic requests both AVERAGE and SAMPLE_COUNT statistics for AVERAGE`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())

        client().getMetricStatistic("ns", "Mem", emptyList(), Statistic.AVERAGE, period)

        val captor = argumentCaptor<Consumer<GetMetricStatisticsRequest.Builder>>()
        verify(sdkClient).getMetricStatistics(captor.capture())
        val builder = GetMetricStatisticsRequest.builder()
        captor.firstValue.accept(builder)

        assertEquals(listOf(Statistic.AVERAGE, Statistic.SAMPLE_COUNT), builder.build().statistics())
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
        // 10-day, ~24h-short range / 60 divisions, rounded UP to a valid 60s resolution (<=15 days old) -> 15840
        assertEquals(15840, request.period())
    }

    @Test
    fun `getMetricStatistic rounds the period up to a 300s resolution for data older than 15 days`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())
        // period.start 2025-01-10 is ~41 days before this clock -> 300s resolution
        val clock = Clock.fixed(Instant.parse("2025-02-20T00:00:00Z"), ZoneOffset.UTC)

        client(clock).getMetricStatistic("ns", "Mem", emptyList(), Statistic.MAXIMUM, period)

        val captor = argumentCaptor<Consumer<GetMetricStatisticsRequest.Builder>>()
        verify(sdkClient).getMetricStatistics(captor.capture())
        val builder = GetMetricStatisticsRequest.builder()
        captor.firstValue.accept(builder)

        // ceil(950399/60 / 300) * 300 = 53 * 300 = 15900
        assertEquals(15900, builder.build().period())
    }

    @Test
    fun `getMetricStatistic rounds the period up to a 3600s resolution for data older than 63 days`() {
        stubResponse(GetMetricStatisticsResponse.builder().datapoints(emptyList()).build())
        // period.start 2025-01-10 is ~142 days before this clock -> 3600s resolution
        val clock = Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC)

        client(clock).getMetricStatistic("ns", "Mem", emptyList(), Statistic.MAXIMUM, period)

        val captor = argumentCaptor<Consumer<GetMetricStatisticsRequest.Builder>>()
        verify(sdkClient).getMetricStatistics(captor.capture())
        val builder = GetMetricStatisticsRequest.builder()
        captor.firstValue.accept(builder)

        // ceil(950399/60 / 3600) * 3600 = 5 * 3600 = 18000
        assertEquals(18000, builder.build().period())
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

    @Test
    fun `getMetricStatistic queries the us-east-1 client for the CloudFront region`() {
        whenever(cloudFrontSdkClient.getMetricStatistics(any<Consumer<GetMetricStatisticsRequest.Builder>>()))
            .thenReturn(
                GetMetricStatisticsResponse
                    .builder()
                    .datapoints(Datapoint.builder().average(0.82).sampleCount(10.0).build())
                    .build(),
            )

        val result =
            client().getMetricStatistic(
                "AWS/CloudFront",
                "4xxErrorRate",
                emptyList(),
                Statistic.AVERAGE,
                period,
                Region.US_EAST_1,
            )

        assertEquals(0.82, result)
        verify(cloudFrontSdkClient).getMetricStatistics(any<Consumer<GetMetricStatisticsRequest.Builder>>())
        verifyNoInteractions(sdkClient)
    }

    @Test
    fun `getMetricStatistic queries the default client when no region is supplied`() {
        stubResponse(
            GetMetricStatisticsResponse
                .builder()
                .datapoints(Datapoint.builder().maximum(50.0).build())
                .build(),
        )

        client().getMetricStatistic("AWS/ECS", "MemoryUtilization", emptyList(), Statistic.MAXIMUM, period)

        verify(sdkClient).getMetricStatistics(any<Consumer<GetMetricStatisticsRequest.Builder>>())
        verifyNoInteractions(cloudFrontSdkClient)
    }
}
