package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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

    private val siteId = "test-site-id"
    private val period =
        ReportingPeriod(
            Instant.parse("2025-01-10T00:00:00Z"),
            Instant.parse("2025-01-20T23:59:59Z"),
        )

    private fun service() = PlausibleMetricsService(plausibleClient, siteId)

    private fun row(
        page: String,
        visitors: Double,
        pageViews: Double = visitors,
    ) = PlausibleResultRow(metrics = listOf(visitors, pageViews), dimensions = listOf(page))

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
    fun `getCompletionRates caps the rate at 100 percent when confirmations exceed starts`() {
        whenever(plausibleClient.query(any())).thenReturn(
            PlausibleQueryResponse(
                listOf(
                    row("/landlord/register-property", visitors = 0.0, pageViews = 50.0),
                    row("/landlord/register-property/confirmation", visitors = 0.0, pageViews = 75.0),
                ),
            ),
        )

        assertEquals(100.0, service().getCompletionRates(period).propertyRegistration)
    }

    @Test
    fun `getCompletionRates queries Plausible with UK date range, visitors and pageViews metrics and page filter`() {
        whenever(plausibleClient.query(any())).thenReturn(PlausibleQueryResponse(emptyList()))

        service().getCompletionRates(period)

        val captor = argumentCaptor<PlausibleQuery>()
        verify(plausibleClient).query(captor.capture())
        val query = captor.firstValue
        assertEquals(siteId, query.siteId)
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
}
