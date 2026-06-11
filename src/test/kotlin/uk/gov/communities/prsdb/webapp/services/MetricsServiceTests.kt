package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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

    private fun stubCounts() {
        whenever(landlordRepository.countByCreatedDateBetween(any(), any())).thenReturn(0L)
        whenever(landlordRepository.countByIsVerifiedTrueAndCreatedDateBetween(any(), any())).thenReturn(0L)
        whenever(propertyOwnershipRepository.countByCreatedDateBetween(any(), any())).thenReturn(0L)
        whenever(propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedBetween(any(), any())).thenReturn(0L)
    }

    private fun durationsOfDays(vararg days: Long): List<Array<Instant>> = days.map { arrayOf(start, start.plus(Duration.ofDays(it))) }

    @Test
    fun `getMetrics returns the counts computed over the period`() {
        whenever(landlordRepository.countByCreatedDateBetween(start, end)).thenReturn(7L)
        whenever(landlordRepository.countByIsVerifiedTrueAndCreatedDateBetween(start, end)).thenReturn(5L)
        whenever(propertyOwnershipRepository.countByCreatedDateBetween(start, end)).thenReturn(4L)
        whenever(propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedBetween(start, end)).thenReturn(3L)
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(start, end))
            .thenReturn(emptyList())

        val metrics = metricsService.getMetrics(period)

        assertEquals(7L, metrics.numberOfLandlordRegistrations)
        assertEquals(5L, metrics.numberOfVerifiedLandlords)
        assertEquals(4L, metrics.numberOfProperties)
        assertEquals(3L, metrics.numberOfLandlordsWithAProperty)
    }

    @Test
    fun `getMetrics returns null time-to-first-property percentiles when there is no data`() {
        stubCounts()
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(any(), any()))
            .thenReturn(emptyList())

        val metrics = metricsService.getMetrics(period)

        assertNull(metrics.medianTimeToFirstProperty)
        assertNull(metrics.p90TimeToFirstProperty)
        assertNull(metrics.p95TimeToFirstProperty)
    }

    @Test
    fun `getMetrics returns the single value for every percentile when there is one data point`() {
        stubCounts()
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(any(), any()))
            .thenReturn(durationsOfDays(7))

        val metrics = metricsService.getMetrics(period)

        assertEquals(Duration.ofDays(7), metrics.medianTimeToFirstProperty)
        assertEquals(Duration.ofDays(7), metrics.p90TimeToFirstProperty)
        assertEquals(Duration.ofDays(7), metrics.p95TimeToFirstProperty)
    }

    @Test
    fun `getMetrics computes median p90 and p95 with linear interpolation`() {
        stubCounts()
        // Eleven values 10,20,...,110 days, supplied unsorted to confirm sorting.
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(any(), any()))
            .thenReturn(durationsOfDays(110, 30, 70, 10, 90, 50, 20, 100, 40, 80, 60))

        val metrics = metricsService.getMetrics(period)

        // rank = fraction * (n - 1) = fraction * 10
        assertEquals(Duration.ofDays(60), metrics.medianTimeToFirstProperty) // rank 5 -> 60
        assertEquals(Duration.ofDays(100), metrics.p90TimeToFirstProperty) // rank 9 -> 100
        assertEquals(Duration.ofDays(105), metrics.p95TimeToFirstProperty) // rank 9.5 -> midway 100..110
    }

    @Test
    fun `getMetrics computes percentiles for realistic mixed minute hour and day durations`() {
        stubCounts()
        // Three landlords whose first property followed registration after 27 minutes,
        // 1 day 3 hours 42 minutes, and 2 days 12 hours 38 minutes respectively.
        whenever(propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(any(), any()))
            .thenReturn(
                listOf(
                    arrayOf(start, start.plus(Duration.ofMinutes(27))),
                    arrayOf(start, start.plus(Duration.ofDays(1)).plus(Duration.ofHours(3)).plus(Duration.ofMinutes(42))),
                    arrayOf(start, start.plus(Duration.ofDays(2)).plus(Duration.ofHours(12)).plus(Duration.ofMinutes(38))),
                ),
            )

        val metrics = metricsService.getMetrics(period)

        // Median (rank 1 of 3) lands exactly on the middle data point.
        assertEquals(
            Duration.ofDays(1).plusHours(3).plusMinutes(42),
            metrics.medianTimeToFirstProperty,
        )
    }
}
