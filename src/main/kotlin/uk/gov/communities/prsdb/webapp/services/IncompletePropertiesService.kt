package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.TrackEmailSentException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalDate
import kotlin.math.ceil

@PrsdbTaskService
class IncompletePropertiesService(
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
    private val reminderEmailSentRepository: ReminderEmailSentRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
) {
    fun getIncompletePropertiesDueReminderPage(
        cutoffDate: Instant,
        page: Int = 0,
    ): List<LandlordIncompleteProperties> =
        landlordIncompletePropertiesRepository
            .findBySavedJourneyState_CreatedDateBefore(
                cutoffDate,
                PageRequest.of(page, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE),
            ).filter { it.savedJourneyState.reminderEmailSent == null }

    fun recordReminderEmailSent(savedJourneyState: SavedJourneyState) {
        try {
            val reminderEmailSentRecord =
                ReminderEmailSent(
                    lastReminderEmailSentDate = Instant.now(),
                )
            reminderEmailSentRepository.save(reminderEmailSentRecord)
            savedJourneyState.reminderEmailSent = reminderEmailSentRecord
            savedJourneyStateRepository.save(savedJourneyState)
        } catch (e: Exception) {
            throw TrackEmailSentException(message = e.message, cause = e.cause)
        }
    }

    fun deleteIncompletePropertiesOlderThan28Days(): Long {
        val cutoffDate = DateTimeHelper.getJavaInstantFromLocalDate(LocalDate.now().minusDays(28))
        val totalPages = getNumberOfPagesOfIncompletePropertiesOlderThanDate(cutoffDate)

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

    fun getNumberOfPagesOfIncompletePropertiesOlderThanDate(cutoffDate: Instant): Int {
        val totalProperties = landlordIncompletePropertiesRepository.countBySavedJourneyState_CreatedDateBefore(cutoffDate).toDouble()
        return ceil(totalProperties / MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE).toInt()
    }
}
