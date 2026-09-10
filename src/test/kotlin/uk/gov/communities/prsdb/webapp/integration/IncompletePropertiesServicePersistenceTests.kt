package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompletePropertiesId
import uk.gov.communities.prsdb.webapp.database.repository.IncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService

class IncompletePropertiesServicePersistenceTests : IntegrationTestWithMutableData("data-old-incomplete-property-with-reminder.sql") {
    @Autowired
    private lateinit var incompletePropertiesRepository: IncompletePropertiesRepository

    @Autowired
    private lateinit var reminderEmailSentRepository: ReminderEmailSentRepository

    @Autowired
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Test
    fun `deleting an old incomplete property also deletes its reminder email record`() {
        val service =
            IncompletePropertiesService(
                incompletePropertiesRepository,
                reminderEmailSentRepository,
                savedJourneyStateRepository,
            )
        var deletedCount = 0L

        TransactionTemplate(transactionManager).executeWithoutResult {
            deletedCount = service.deleteIncompletePropertiesOlderThan28Days()
        }

        assertEquals(1L, deletedCount)
        assertFalse(
            incompletePropertiesRepository.existsById(
                LandlordIncompletePropertiesId(USER_ID, SAVED_JOURNEY_STATE_ID),
            ),
            "Incomplete property was not deleted",
        )
        assertFalse(
            savedJourneyStateRepository.existsById(SAVED_JOURNEY_STATE_ID),
            "Saved journey state was not deleted",
        )
        assertFalse(
            reminderEmailSentRepository.existsById(REMINDER_EMAIL_SENT_ID),
            "Reminder email record was not deleted",
        )
    }

    companion object {
        private const val USER_ID = "test-base-user-id"
        private const val SAVED_JOURNEY_STATE_ID = 1L
        private const val REMINDER_EMAIL_SENT_ID = 1L
    }
}
