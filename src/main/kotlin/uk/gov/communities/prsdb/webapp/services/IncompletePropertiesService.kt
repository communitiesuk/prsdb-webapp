package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalDate

@PrsdbTaskService
class IncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val reminderEmailSentRepository: ReminderEmailSentRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
) {
    fun getIncompletePropertiesDueReminder(): List<LandlordIncompleteProperties> =
        landlordIncompletePropertiesRepository
            .findBySavedJourneyState_CreatedDateBefore(
                DateTimeHelper.getJavaInstantFromLocalDate(
                    LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
                ),
            ).filter { it.savedJourneyState.reminderEmailSent == null }

    fun recordReminderEmailSent(savedJourneyState: SavedJourneyState) {
        val reminderEmailSentRecord =
            ReminderEmailSent(
                lastReminderEmailSentDate = Instant.now(),
            )
        reminderEmailSentRepository.save(reminderEmailSentRecord)
        savedJourneyState.reminderEmailSent = reminderEmailSentRecord
        savedJourneyStateRepository.save(savedJourneyState)
    }
}
