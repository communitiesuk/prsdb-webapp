package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompletePropertiesId
import uk.gov.communities.prsdb.webapp.database.repository.IncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData.Companion.createReminderEmailSent
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData.Companion.createSavedJourneyState
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

class IncompletePropertiesServicePersistenceTests : IntegrationTestWithMutableData(emptyList()) {
    @Autowired
    private lateinit var incompletePropertiesRepository: IncompletePropertiesRepository

    @Autowired
    private lateinit var prsdbUserRepository: PrsdbUserRepository

    @Autowired
    private lateinit var reminderEmailSentRepository: ReminderEmailSentRepository

    @Autowired
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Test
    fun `deleting an old incomplete property also deletes its reminder email record`() {
        val user = prsdbUserRepository.save(createPrsdbUser())
        val reminderEmailSent = reminderEmailSentRepository.save(createReminderEmailSent())
        val savedJourneyState =
            savedJourneyStateRepository.save(
                createSavedJourneyState(
                    baseUser = user,
                    reminderEmailSent = reminderEmailSent,
                    entityId = 0,
                ),
            )
        incompletePropertiesRepository.save(LandlordIncompleteProperties(user, savedJourneyState))
        val incompletePropertyId = LandlordIncompletePropertiesId(user.id, savedJourneyState.id)

        // This needs to be old so deleteIncompletePropertiesOlderThan28Days will try to delete it
        jdbcTemplate.update(
            "UPDATE saved_journey_state SET created_date = ? WHERE id = ?",
            Timestamp.from(Instant.now().minus(29, ChronoUnit.DAYS)),
            savedJourneyState.id,
        )

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
            incompletePropertiesRepository.existsById(incompletePropertyId),
            "Incomplete property was not deleted",
        )
        assertFalse(
            savedJourneyStateRepository.existsById(savedJourneyState.id),
            "Saved journey state was not deleted",
        )
        assertFalse(
            reminderEmailSentRepository.existsById(reminderEmailSent.id),
            "Reminder email record was not deleted",
        )
    }
}
