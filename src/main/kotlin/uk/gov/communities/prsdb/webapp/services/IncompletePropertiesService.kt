package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalDate
import kotlin.math.ceil

@PrsdbTaskService
class IncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
    private val reminderEmailSentRepository: ReminderEmailSentRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
) {
    fun getIncompletePropertiesDueReminder(page: Int = 0): List<LandlordIncompleteProperties> {
        // TODO PDJB-340 after PRSD-1030
        // Refactor so that all pages are processed / reminder emails sent.
        // Do we want to do something if we hit the notify daily email limit part way through sending these emails?
        val cutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        return landlordIncompletePropertiesRepository
            .findBySavedJourneyState_CreatedDateBefore(
                cutoffDate,
                PageRequest.of(page, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE),
            ).filter { it.savedJourneyState.reminderEmailSent == null }
    }

    fun recordReminderEmailSent(savedJourneyState: SavedJourneyState) {
        val reminderEmailSentRecord =
            ReminderEmailSent(
                lastReminderEmailSentDate = Instant.now(),
            )
        reminderEmailSentRepository.save(reminderEmailSentRecord)
        savedJourneyState.reminderEmailSent = reminderEmailSentRecord
        savedJourneyStateRepository.save(savedJourneyState)
    }

    fun deleteIncompletePropertiesOlderThan28Days(): Long {
        val cutoffDate = DateTimeHelper.getJavaInstantFromLocalDate(LocalDate.now().minusDays(28))
        val totalPages = getTotalPagesOfIncompletePropertiesOlderThanDate(cutoffDate)

        var totalDeleted = 0L

        // Delete in reverse order to avoid issues with pagination changing as we delete records
        for (page in (totalPages - 1) downTo 0) {
            val incompletePropertiesBatch =
                landlordIncompletePropertiesRepository
                    .findBySavedJourneyState_CreatedDateBefore(
                        cutoffDate,
                        PageRequest.of(page, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE),
                    )

            val journeyStatesToDelete = incompletePropertiesBatch.map { it.savedJourneyState }
            savedJourneyStateRepository.deleteAll(journeyStatesToDelete)

            totalDeleted += journeyStatesToDelete.size
        }

        return totalDeleted
    }

    fun getTotalPagesOfIncompletePropertiesOlderThanDate(cutoffDate: Instant): Int {
        val totalProperties = landlordIncompletePropertiesRepository.countBySavedJourneyState_CreatedDateBefore(cutoffDate).toDouble()
        return ceil((totalProperties / MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE)).toInt()
    }
}
