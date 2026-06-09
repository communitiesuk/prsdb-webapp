package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class MetricsServiceTests {
    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @InjectMocks
    private lateinit var metricsService: MetricsService

    private val start = Instant.parse("2025-01-01T00:00:00Z")
    private val end = Instant.parse("2025-01-31T23:59:59Z")
    private val period = ReportingPeriod(start, end)

    @Test
    fun `getMetrics returns the four computed metrics over the period`() {
        whenever(landlordRepository.countByCreatedDateBetween(start, end)).thenReturn(7L)
        whenever(propertyOwnershipRepository.countByCreatedDateBetween(start, end)).thenReturn(4L)
        whenever(propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedOnOrBefore(end)).thenReturn(3L)
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(start, end))
            .thenReturn(
                listOf(
                    // 2 days
                    arrayOf(Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-03T00:00:00Z")),
                    // 4 days
                    arrayOf(Instant.parse("2025-01-05T00:00:00Z"), Instant.parse("2025-01-09T00:00:00Z")),
                ),
            )

        val metrics = metricsService.getMetrics(period)

        assertEquals(7L, metrics.numberOfLandlordRegistrations)
        assertEquals(4L, metrics.numberOfProperties)
        assertEquals(3L, metrics.numberOfLandlordsWithAProperty)
        assertEquals(Duration.ofDays(3), metrics.averageTimeToFirstProperty) // mean of 2 and 4 days
    }

    @Test
    fun `getMetrics returns a null average when no landlords qualify`() {
        whenever(landlordRepository.countByCreatedDateBetween(start, end)).thenReturn(0L)
        whenever(propertyOwnershipRepository.countByCreatedDateBetween(start, end)).thenReturn(0L)
        whenever(propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedOnOrBefore(end)).thenReturn(0L)
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(start, end))
            .thenReturn(emptyList())

        val metrics = metricsService.getMetrics(period)

        assertNull(metrics.averageTimeToFirstProperty)
    }

    @Test
    fun `getMetrics averages sub-day durations`() {
        whenever(landlordRepository.countByCreatedDateBetween(start, end)).thenReturn(1L)
        whenever(propertyOwnershipRepository.countByCreatedDateBetween(start, end)).thenReturn(1L)
        whenever(propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedOnOrBefore(end)).thenReturn(1L)
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(start, end))
            .thenReturn(
                listOf(
                    // 2 hours
                    arrayOf(Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T02:00:00Z")),
                    // 6 hours
                    arrayOf(Instant.parse("2025-01-02T00:00:00Z"), Instant.parse("2025-01-02T06:00:00Z")),
                ),
            )

        val metrics = metricsService.getMetrics(period)

        assertEquals(Duration.ofHours(4), metrics.averageTimeToFirstProperty)
    }
}
