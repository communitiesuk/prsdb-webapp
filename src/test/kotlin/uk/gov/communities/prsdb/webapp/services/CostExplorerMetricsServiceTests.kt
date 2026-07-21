package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.exception.SdkException
import uk.gov.communities.prsdb.webapp.clients.CostExplorerMetricsClient
import uk.gov.communities.prsdb.webapp.models.dataModels.CostExplorerCostDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.CostMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class CostExplorerMetricsServiceTests {
    @Mock
    private lateinit var client: CostExplorerMetricsClient

    private val period =
        ReportingPeriod(Instant.parse("2025-01-10T00:00:00Z"), Instant.parse("2025-01-20T23:59:59Z"))

    private fun service() = CostExplorerMetricsService(client)

    @Test
    fun `getMetrics returns total cost per transaction currency and final status`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("123.45"), "USD", false))

        val result = service().getMetrics(period, 753)

        assertEquals(
            CostMetricsDataModel(
                totalCost = BigDecimal("123.45"),
                costPerTransaction = BigDecimal("0.16"),
                currencyCode = "USD",
                isEstimated = false,
            ),
            result,
        )
        verify(client).getCost(period)
    }

    @Test
    fun `getMetrics rounds cost per transaction half up to two decimal places`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("1.00"), "USD", false))

        val result = service().getMetrics(period, 8)

        assertEquals(BigDecimal("0.13"), result.costPerTransaction)
    }

    @Test
    fun `getMetrics preserves cost details without a cost per transaction when transaction count is zero`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("123.45"), "USD", true))

        val result = service().getMetrics(period, 0)

        assertEquals(
            CostMetricsDataModel(totalCost = BigDecimal("123.45"), currencyCode = "USD", isEstimated = true),
            result,
        )
    }

    @Test
    fun `getMetrics leaves cost per transaction null when transaction count is negative`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("123.45"), "USD", false))

        assertEquals(null, service().getMetrics(period, -1).costPerTransaction)
    }

    @Test
    fun `getMetrics retains zero source amounts with positive transaction counts`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("0.00"), "USD", false))

        val result = service().getMetrics(period, 5)

        assertEquals(
            CostMetricsDataModel(
                totalCost = BigDecimal("0.00"),
                costPerTransaction = BigDecimal("0.00"),
                currencyCode = "USD",
                isEstimated = false,
            ),
            result,
        )
    }

    @Test
    fun `getMetrics retains negative source amounts and calculates negative cost per transaction`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("-1.00"), "USD", false))

        val result = service().getMetrics(period, 8)

        assertEquals(
            CostMetricsDataModel(
                totalCost = BigDecimal("-1.00"),
                costPerTransaction = BigDecimal("-0.13"),
                currencyCode = "USD",
            ),
            result,
        )
    }

    @Test
    fun `getMetrics returns no data when the client returns null`() {
        whenever(client.getCost(period)).thenReturn(null)

        assertEquals(CostMetricsDataModel(), service().getMetrics(period, 10))
    }

    @Test
    fun `getMetrics returns no data when the client throws an SdkClientException`() {
        whenever(client.getCost(period)).thenThrow(SdkClientException.create("AWS unavailable"))

        assertEquals(CostMetricsDataModel(), service().getMetrics(period, 10))
    }

    @Test
    fun `getMetrics returns no data when the client throws an SdkException`() {
        whenever(client.getCost(period)).thenThrow(SdkException.builder().message("AWS unavailable").build())

        assertEquals(CostMetricsDataModel(), service().getMetrics(period, 10))
    }

    @Test
    fun `getMetrics propagates an unexpected RuntimeException from the client`() {
        val exception = RuntimeException("Unexpected failure")
        whenever(client.getCost(period)).thenThrow(exception)

        assertEquals(exception, assertFailsWith<RuntimeException> { service().getMetrics(period, 10) })
    }

    @Test
    fun `getMetrics copies estimated status from the cost data`() {
        whenever(client.getCost(period)).thenReturn(CostExplorerCostDataModel(BigDecimal("1.00"), "USD", true))

        assertEquals(true, service().getMetrics(period, 1).isEstimated)
    }
}
