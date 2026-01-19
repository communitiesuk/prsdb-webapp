package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertyForReminderDataModel
import java.time.LocalDate

@PrsdbTaskService
class IncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) {
    fun getIncompletePropertyReminders(): List<IncompletePropertyForReminderDataModel> {
        val incompleteProperties =
            landlordIncompletePropertiesRepository.findBySavedJourneyState_CreatedDateBefore(
                DateTimeHelper.getJavaInstantFromLocalDate(
                    LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
                ),
            )

        return incompleteProperties.map { incompleteProperty ->
            IncompletePropertyForReminderDataModel(
                incompleteProperty.landlord.email,
                incompleteProperty.savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(incompleteProperty.savedJourneyState),
                incompleteProperty.savedJourneyState.journeyId,
            )
        }
    }
}
