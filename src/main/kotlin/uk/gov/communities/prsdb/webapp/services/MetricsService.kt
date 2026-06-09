package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.time.Duration

@PrsdbWebService
class MetricsService(
    private val landlordRepository: LandlordRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    @Transactional
    fun getMetrics(period: ReportingPeriod): MetricsDataModel =
        MetricsDataModel(
            numberOfLandlordRegistrations =
                landlordRepository.countByCreatedDateBetween(period.start, period.end),
            numberOfProperties =
                propertyOwnershipRepository.countByCreatedDateBetween(period.start, period.end),
            numberOfLandlordsWithAProperty =
                propertyOwnershipRepository.countDistinctLandlordsWithPropertyCreatedOnOrBefore(period.end),
            averageTimeToFirstProperty = getAverageTimeToFirstProperty(period),
        )

    private fun getAverageTimeToFirstProperty(period: ReportingPeriod): Duration? {
        val registrationAndFirstPropertyDates =
            propertyOwnershipRepository.findLandlordAndFirstPropertyCreatedDates(period.start, period.end)
        if (registrationAndFirstPropertyDates.isEmpty()) {
            return null
        }
        val totalSeconds =
            registrationAndFirstPropertyDates.sumOf { (landlordCreatedDate, firstPropertyCreatedDate) ->
                Duration.between(landlordCreatedDate, firstPropertyCreatedDate).seconds
            }
        return Duration.ofSeconds(totalSeconds / registrationAndFirstPropertyDates.size)
    }
}
