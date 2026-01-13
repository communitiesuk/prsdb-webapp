package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesForReminderDataModel
import java.time.LocalDate

@PrsdbTaskService
class LandlordIncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) {
    fun getIncompletePropertiesOlderThanDays(days: Int): List<IncompletePropertiesForReminderDataModel> {
        val incompleteProperties =
            landlordIncompletePropertiesRepository.findBySavedJourneyState_CreatedDateBefore(
                DateTimeHelper.getJavaInstantFromLocalDate(LocalDate.now().minusDays(days.toLong())),
            )

        return incompleteProperties.map { incompleteProperty ->
            IncompletePropertiesForReminderDataModel(
                incompleteProperty.landlord.email,
                incompleteProperty.savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                CompleteByDateHelper.getIncompletePropertyCompleteByDate(incompleteProperty.savedJourneyState),
            )
        }
    }
}
