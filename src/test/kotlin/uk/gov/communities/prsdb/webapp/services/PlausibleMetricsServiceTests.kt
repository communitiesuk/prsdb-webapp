package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.clients.PlausibleClient
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQuery
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQueryResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleResultRow
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PlausibleMetricsServiceTests {
    @Mock
    private lateinit var plausibleClient: PlausibleClient

    private val domainId = "test-domain-id"
    private val period =
        ReportingPeriod(
            Instant.parse("2025-01-10T00:00:00Z"),
            Instant.parse("2025-01-20T23:59:59Z"),
        )

    private fun service(transactionEventStartDate: String = "2099-01-01") =
        PlausibleMetricsService(plausibleClient, domainId, transactionEventStartDate)

    private fun stubByEventName(
        flowCount: Double,
        transactionCount: Double,
    ) {
        whenever(plausibleClient.query(any())).thenAnswer { invocation ->
            val query = invocation.getArgument<PlausibleQuery>(0)
            val isTransactionEventQuery = query.filters.toString().contains("event:name")
            val count = if (isTransactionEventQuery) transactionCount else flowCount
            PlausibleQueryResponse(listOf(aggregateRow(count)))
        }
    }

    private fun row(
        page: String,
        visitors: Double,
        pageViews: Double = visitors,
    ) = PlausibleResultRow(metrics = listOf(visitors, pageViews), dimensions = listOf(page))

    private fun aggregateRow(count: Double) = PlausibleResultRow(metrics = listOf(count), dimensions = emptyList())

    @Test
    fun `getCompletionRates uses visitors for landlord and council and pageViews for property`() {
        whenever(plausibleClient.query(any())).thenReturn(
            PlausibleQueryResponse(
                listOf(
                    row("/landlord/register-as-a-landlord/start", visitors = 1000.0, pageViews = 9999.0),
                    row("/landlord/register-as-a-landlord/confirmation", visitors = 732.0, pageViews = 9999.0),
                    row("/landlord/register-property", visitors = 9999.0, pageViews = 80.0),
                    row("/landlord/register-property/confirmation", visitors = 9999.0, pageViews = 20.0),
                    row("/local-council/register-local-council-user/privacy-notice", visitors = 3.0, pageViews = 9999.0),
                    row("/local-council/register-local-council-user/confirmation", visitors = 1.0, pageViews = 9999.0),
                ),
            ),
        )

        val rates = service().getCompletionRates(period)

        assertEquals(73.20, rates.landlordRegistration)
        assertEquals(25.00, rates.propertyRegistration)
        assertEquals(33.33, rates.localCouncilUserRegistration)
    }

    @Test
    fun `getCompletionRates returns null for a journey with zero start visitors`() {
        whenever(plausibleClient.query(any())).thenReturn(
            PlausibleQueryResponse(
                listOf(row("/landlord/register-as-a-landlord/start", visitors = 0.0)),
            ),
        )

        assertNull(service().getCompletionRates(period).landlordRegistration)
    }

    @Test
    fun `getCompletionRates returns null for a journey missing from the results`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(emptyList()))

        val rates = service().getCompletionRates(period)

        assertNull(rates.landlordRegistration)
        assertNull(rates.propertyRegistration)
        assertNull(rates.localCouncilUserRegistration)
    }

    @Test
    fun `getCompletionRates returns zero when there are start page views but no confirmations`() {
        whenever(plausibleClient.query(any())).thenReturn(
            PlausibleQueryResponse(
                listOf(row("/landlord/register-property", visitors = 0.0, pageViews = 50.0)),
            ),
        )

        assertEquals(0.0, service().getCompletionRates(period).propertyRegistration)
    }

    @Test
    fun `getCompletionRates returns all null rates when the client throws`() {
        whenever(plausibleClient.query(any())).thenThrow(RuntimeException("boom"))

        val rates = service().getCompletionRates(period)

        assertNull(rates.landlordRegistration)
        assertNull(rates.propertyRegistration)
        assertNull(rates.localCouncilUserRegistration)
    }

    @Test
    fun `getCompletionRates does not cap the rate at 100 percent when confirmations exceed starts`() {
        whenever(plausibleClient.query(any())).thenReturn(
            PlausibleQueryResponse(
                listOf(
                    row("/landlord/register-property", visitors = 0.0, pageViews = 50.0),
                    row("/landlord/register-property/confirmation", visitors = 0.0, pageViews = 75.0),
                ),
            ),
        )

        assertEquals(150.0, service().getCompletionRates(period).propertyRegistration)
    }

    @Test
    fun `getCompletionRates queries Plausible with UK date range, visitors and pageViews metrics and page filter`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(emptyList()))

        service().getCompletionRates(period)

        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient).query(captor.capture())
        val query = captor.firstValue
        assertEquals(domainId, query.siteId)
        assertEquals(listOf("2025-01-10", "2025-01-20"), query.dateRange)
        assertEquals(listOf("visitors", "pageviews"), query.metrics)
        assertEquals(listOf("event:page"), query.dimensions)
        val filter = query.filters.single()
        assertEquals("is", filter[0])
        assertEquals("event:page", filter[1])
        @Suppress("UNCHECKED_CAST")
        val pages = filter[2] as List<String>
        assertEquals(6, pages.size)
        assertTrue(pages.contains("/landlord/register-as-a-landlord/confirmation"))
    }

    @Test
    fun `getTransactionCounts returns the aggregate events total as a Long`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(listOf(aggregateRow(842.0))))

        assertEquals(842L, service().getTransactionCounts(period))
    }

    @Test
    fun `getTransactionCounts returns zero when there are no results`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(emptyList()))

        assertEquals(0L, service().getTransactionCounts(period))
    }

    @Test
    fun `getTransactionCounts returns zero when the client throws`() {
        whenever(plausibleClient.query(any())).thenThrow(RuntimeException("boom"))

        assertEquals(0L, service().getTransactionCounts(period))
    }

    @Test
    fun `getTransactionCounts queries Plausible with the events metric, no dimensions and a Flow props filter`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(emptyList()))

        service().getTransactionCounts(period)

        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient).query(captor.capture())
        val query = captor.firstValue
        assertEquals(domainId, query.siteId)
        assertEquals(listOf("2025-01-10", "2025-01-20"), query.dateRange)
        assertEquals(listOf("events"), query.metrics)
        assertTrue(query.dimensions.isEmpty())
        val filter = query.filters.single()
        assertEquals("or", filter[0])
        val serialised = query.filters.toString()
        assertTrue(serialised.contains("event:props:currentUrl"))
        assertTrue(serialised.contains("event:props:referrer"))
        assertTrue(serialised.contains("/landlord/register-as-a-landlord/confirmation"))
        assertTrue(serialised.contains("check-gas-safety-answers"))
        assertTrue(!serialised.contains("update-bedrooms"))
    }

    @Test
    fun `getTransactionCounts uses only the Flow query when the period ends before the cutover`() {
        stubByEventName(flowCount = 100.0, transactionCount = 999.0)

        val count =
            PlausibleMetricsService(plausibleClient, domainId, "2025-02-01")
                .getTransactionCounts(period)

        assertEquals(100L, count)
        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient).query(captor.capture())
        assertTrue(captor.firstValue.filters.toString().contains("event:props:currentUrl"))
        assertEquals(listOf("2025-01-10", "2025-01-20"), captor.firstValue.dateRange)
    }

    @Test
    fun `getTransactionCounts uses only the Transaction event query when the period starts on or after the cutover`() {
        stubByEventName(flowCount = 999.0, transactionCount = 250.0)

        val count =
            PlausibleMetricsService(plausibleClient, domainId, "2025-01-01")
                .getTransactionCounts(period)

        assertEquals(250L, count)
        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient).query(captor.capture())
        val query = captor.firstValue
        assertEquals(listOf("events"), query.metrics)
        assertTrue(query.dimensions.isEmpty())
        assertEquals(listOf("2025-01-10", "2025-01-20"), query.dateRange)
        val serialised = query.filters.toString()
        assertTrue(serialised.contains("event:name"))
        assertTrue(serialised.contains("Transaction"))
    }

    @Test
    fun `getTransactionCounts sums Flow before the cutover and Transaction events on or after it`() {
        stubByEventName(flowCount = 30.0, transactionCount = 12.0)

        val count =
            PlausibleMetricsService(plausibleClient, domainId, "2025-01-15")
                .getTransactionCounts(period)

        assertEquals(42L, count)

        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient, times(2)).query(captor.capture())
        val flowQuery = captor.allValues.single { !it.filters.toString().contains("event:name") }
        val txnQuery = captor.allValues.single { it.filters.toString().contains("event:name") }
        assertEquals(listOf("2025-01-10", "2025-01-14"), flowQuery.dateRange)
        assertEquals(listOf("2025-01-15", "2025-01-20"), txnQuery.dateRange)
    }

    @Test
    fun `getTransactionCounts returns zero when a split query throws`() {
        whenever(plausibleClient.query(any())).thenThrow(RuntimeException("boom"))

        val count =
            PlausibleMetricsService(plausibleClient, domainId, "2025-01-15")
                .getTransactionCounts(period)

        assertEquals(0L, count)
    }
}
