package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Duration
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong

@PrsdbWebService
class MetricsService(
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    @Transactional
    fun getMetrics(period: ReportingPeriod): MetricsDataModel {
        val timesToFirstPropertyOwnership = getTimesToFirstPropertyOwnership(period)
        return MetricsDataModel(
            numberOfLandlordRegistrations =
                individualLandlordRepository.countByCreatedDateBetween(period.start, period.end),
            numberOfVerifiedLandlords =
                individualLandlordRepository.countByIsVerifiedTrueAndCreatedDateBetween(period.start, period.end),
            numberOfProperties =
                propertyOwnershipRepository.countByCreatedDateBetween(period.start, period.end),
            numberOfLandlordsWithAProperty =
                propertyOwnershipRepository.countDistinctLandlordsWithOwnershipLinkCreatedBetween(
                    period.start,
                    period.end,
                ),
            medianTimeToFirstProperty = percentile(timesToFirstPropertyOwnership, 0.5),
            p90TimeToFirstProperty = percentile(timesToFirstPropertyOwnership, 0.9),
            p95TimeToFirstProperty = percentile(timesToFirstPropertyOwnership, 0.95),
        )
    }

    private fun getTimesToFirstPropertyOwnership(period: ReportingPeriod): List<Duration> =
        propertyOwnershipRepository
            .findLandlordAndFirstOwnershipLinkCreatedDates(period.start, period.end)
            .map { (landlordCreatedDate, firstOwnershipLinkCreatedDate) ->
                Duration.between(landlordCreatedDate, firstOwnershipLinkCreatedDate)
            }

    private fun percentile(
        durations: List<Duration>,
        fraction: Double,
    ): Duration? {
        if (durations.isEmpty()) return null
        val sortedSeconds = durations.map { it.seconds }.sorted()
        val rank = fraction * (sortedSeconds.size - 1)
        val lowerIndex = floor(rank).toInt()
        val upperIndex = ceil(rank).toInt()
        if (lowerIndex == upperIndex) {
            return Duration.ofSeconds(sortedSeconds[lowerIndex])
        }
        val lowerValue = sortedSeconds[lowerIndex]
        val upperValue = sortedSeconds[upperIndex]
        val interpolated = lowerValue + (rank - lowerIndex) * (upperValue - lowerValue)
        return Duration.ofSeconds(interpolated.roundToLong())
    }
}
