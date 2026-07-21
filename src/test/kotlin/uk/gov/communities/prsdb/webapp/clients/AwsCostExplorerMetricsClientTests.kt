package uk.gov.communities.prsdb.webapp.clients

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import software.amazon.awssdk.services.costexplorer.model.CostExplorerException
import software.amazon.awssdk.services.costexplorer.model.DateInterval
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageRequest
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageResponse
import software.amazon.awssdk.services.costexplorer.model.Granularity
import software.amazon.awssdk.services.costexplorer.model.MetricValue
import software.amazon.awssdk.services.costexplorer.model.ResultByTime
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AwsCostExplorerMetricsClientTests {
    @Mock
    private lateinit var sdkClient: CostExplorerClient

    private val period =
        ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-20T23:59:59Z"))

    private fun client() = AwsCostExplorerMetricsClient(sdkClient)

    private fun metric(
        amount: String,
        unit: String = "USD",
    ): MetricValue = MetricValue.builder().amount(amount).unit(unit).build()

    private fun result(
        amount: String,
        unit: String = "USD",
        estimated: Boolean = false,
    ): ResultByTime =
        ResultByTime
            .builder()
            .total(mapOf("UnblendedCost" to metric(amount, unit)))
            .estimated(estimated)
            .build()

    private fun response(
        vararg results: ResultByTime,
        nextPageToken: String? = null,
    ): GetCostAndUsageResponse = GetCostAndUsageResponse.builder().resultsByTime(results.toList()).nextPageToken(nextPageToken).build()

    private fun stubResponse(response: GetCostAndUsageResponse) {
        whenever(sdkClient.getCostAndUsage(any<GetCostAndUsageRequest>())).thenReturn(response)
    }

    @Test
    fun `getCost requests an exclusive end date for an inclusive reporting period`() {
        stubResponse(response())

        client().getCost(period)

        val requestCaptor = argumentCaptor<GetCostAndUsageRequest>()
        verify(sdkClient).getCostAndUsage(requestCaptor.capture())
        assertEquals(
            DateInterval.builder().start("2025-01-10").end("2025-01-21").build(),
            requestCaptor.firstValue.timePeriod(),
        )
    }

    @Test
    fun `getCost maps summer reporting period instants to Europe London dates`() {
        val summerPeriod =
            ReportingPeriod(Instant.parse("2025-07-01T23:30:00Z"), Instant.parse("2025-07-31T23:30:00Z"))
        stubResponse(response())

        client().getCost(summerPeriod)

        val requestCaptor = argumentCaptor<GetCostAndUsageRequest>()
        verify(sdkClient).getCostAndUsage(requestCaptor.capture())
        assertEquals(
            DateInterval.builder().start("2025-07-02").end("2025-08-02").build(),
            requestCaptor.firstValue.timePeriod(),
        )
    }

    @Test
    fun `getCost requests daily unblended account costs without filters or grouping`() {
        stubResponse(response())

        client().getCost(period)

        val requestCaptor = argumentCaptor<GetCostAndUsageRequest>()
        verify(sdkClient).getCostAndUsage(requestCaptor.capture())
        val request = requestCaptor.firstValue
        assertEquals(Granularity.DAILY, request.granularity())
        assertEquals(listOf("UnblendedCost"), request.metrics())
        assertNull(request.billingViewArn())
        assertNull(request.filter())
        assertEquals(emptyList(), request.groupBy())
    }

    @Test
    fun `getCost sums daily values without losing decimal precision`() {
        stubResponse(response(result("0.123456789012345678"), result("1.876543210987654322")))

        val result = client().getCost(period)

        assertEquals(BigDecimal("2.000000000000000000"), result?.amount)
        assertEquals("USD", result?.currencyCode)
    }

    @Test
    fun `getCost marks the result estimated when any daily result is estimated`() {
        stubResponse(response(result("1.00"), result("2.00", estimated = true)))

        val result = client().getCost(period)

        assertTrue(result!!.isEstimated)
    }

    @Test
    fun `getCost marks the result not estimated when no daily result is estimated`() {
        stubResponse(response(result("1.00"), result("2.00")))

        val result = client().getCost(period)

        assertFalse(result!!.isEstimated)
    }

    @Test
    fun `getCost follows pagination token and includes results from all pages`() {
        whenever(sdkClient.getCostAndUsage(any<GetCostAndUsageRequest>())).thenReturn(
            response(result("1.20"), nextPageToken = "next-page"),
            response(result("2.30")),
        )

        val result = client().getCost(period)

        val requestCaptor = argumentCaptor<GetCostAndUsageRequest>()
        verify(sdkClient, times(2)).getCostAndUsage(requestCaptor.capture())
        assertNull(requestCaptor.firstValue.nextPageToken())
        assertEquals("next-page", requestCaptor.secondValue.nextPageToken())
        assertEquals(BigDecimal("3.50"), result?.amount)
    }

    @Test
    fun `getCost returns null when Cost Explorer returns no results`() {
        stubResponse(response())

        assertNull(client().getCost(period))
    }

    @Test
    fun `getCost returns null when a result omits unblended cost`() {
        stubResponse(
            response(
                ResultByTime.builder().total(mapOf("BlendedCost" to metric("1.00"))).estimated(false).build(),
            ),
        )

        assertNull(client().getCost(period))
    }

    @Test
    fun `getCost returns null when an amount is malformed`() {
        stubResponse(response(result("not-a-decimal")))

        assertNull(client().getCost(period))
    }

    @Test
    fun `getCost returns null rather than a partial sum when a later page has a malformed amount`() {
        whenever(sdkClient.getCostAndUsage(any<GetCostAndUsageRequest>())).thenReturn(
            response(result("1.00"), nextPageToken = "next-page"),
            response(result("not-a-decimal")),
        )

        assertNull(client().getCost(period))

        verify(sdkClient, times(2)).getCostAndUsage(any<GetCostAndUsageRequest>())
    }

    @Test
    fun `getCost returns null when a currency unit is blank`() {
        stubResponse(response(result("1.00", unit = " ")))

        assertNull(client().getCost(period))
    }

    @Test
    fun `getCost returns null when currency units are inconsistent`() {
        stubResponse(response(result("1.00", unit = "USD"), result("2.00", unit = "GBP")))

        assertNull(client().getCost(period))
    }

    @Test
    fun `getCost retains valid zero and negative amounts`() {
        stubResponse(response(result("0"), result("-1.25")))

        val result = client().getCost(period)

        assertEquals(BigDecimal("-1.25"), result?.amount)
    }

    @Test
    fun `getCost propagates AWS SDK exceptions`() {
        whenever(sdkClient.getCostAndUsage(any<GetCostAndUsageRequest>())).thenThrow(
            CostExplorerException.builder().message("AWS unavailable").build(),
        )

        assertFailsWith<CostExplorerException> { client().getCost(period) }
    }
}
