package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.RemindableEntityType
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertyForReminderDataModel
import java.time.Instant
import java.time.LocalDate

@PrsdbTaskService
class IncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val reminderEmailSentRepository: ReminderEmailSentRepository,
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
                incompleteProperty.savedJourneyState.id,
            )
        }
    }

    fun recordReminderEmailSent(property: IncompletePropertyForReminderDataModel) {
        val reminderEmailSentRecord =
            ReminderEmailSent(
                lastEmailSentDate = Instant.now(),
                entityType = RemindableEntityType.SAVED_JOURNEY_STATE,
                entityId = property.savedJourneyStateId,
            )
        reminderEmailSentRepository.save(reminderEmailSentRecord)
    }

    fun getIdsOfPropertiesWhichHaveHadRemindersSent(savedJourneyStateIds: List<Long>): List<Long> {
        // TODO PRSD-1030 - do we need to pass in savedJourneyStateIds?
        // Can we delete these records when the relevant saved journey state is deleted?
        // Might need to change the entity to properly FK to saved journey state so we can use orphan removal
        val sentReminders =
            reminderEmailSentRepository.findByEntityTypeAndEntityIdIn(
                RemindableEntityType.SAVED_JOURNEY_STATE,
                savedJourneyStateIds,
            )
        return sentReminders.map { it.entityId }
    }
}
