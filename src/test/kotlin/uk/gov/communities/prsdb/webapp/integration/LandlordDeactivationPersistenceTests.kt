package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.application.DeactivateInactiveLandlordsTaskLogic
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.services.DeactivationService
import java.time.LocalDate

class LandlordDeactivationPersistenceTests : IntegrationTestWithMutableData("data-landlords-for-deactivation-task.sql") {
    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `no registered property query only returns active landlords created before the cutoff with no property`() {
        val cutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusMonths(LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()

        val ids = landlordRepository.findAllByIsActiveTrueAndHasRegisteredPropertyFalseAndCreatedDateBefore(cutoff).map { it.id }

        assertEquals(listOf(OLD_LANDLORD_WITH_NO_PROPERTY_ID), ids)
    }

    @Test
    fun `no active properties query only returns active landlords whose properties are all inactive`() {
        val ids = landlordRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties()

        assertEquals(listOf(LANDLORD_WITH_ONLY_INACTIVE_PROPERTY_ID), ids)
    }

    @Test
    fun `deactivateInactiveLandlords deactivates only the landlords that are due and is idempotent`() {
        val taskLogic =
            DeactivateInactiveLandlordsTaskLogic(
                DeactivationService(
                    propertyOwnershipRepository,
                    landlordRepository,
                    mock(),
                    mock(),
                    transactionTemplate,
                    mock(),
                    mock(),
                ),
            )

        assertEquals(0, taskLogic.deactivateInactiveLandlords())

        val today = LocalDate.now(DateTimeHelper.UK_ZONE)
        listOf(OLD_LANDLORD_WITH_NO_PROPERTY_ID, LANDLORD_WITH_ONLY_INACTIVE_PROPERTY_ID).forEach { id ->
            val landlord = landlordRepository.findById(id).get()
            assertFalse(landlord.isActive, "Landlord $id should be inactive")
            assertEquals(today, landlord.lastDeactivatedDate)
        }
        listOf(BOUNDARY_LANDLORD_WITH_NO_PROPERTY_ID, LANDLORD_WITH_ACTIVE_PROPERTY_ID, LEGACY_LANDLORD_WITH_ACTIVE_PROPERTY_ID)
            .forEach { id -> assertTrue(landlordRepository.findById(id).get().isActive, "Landlord $id should still be active") }
        assertEquals(LocalDate.of(2026, 1, 1), landlordRepository.findById(ALREADY_INACTIVE_LANDLORD_ID).get().lastDeactivatedDate)

        assertEquals(0, taskLogic.deactivateInactiveLandlords())
        assertTrue(landlordRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties().isEmpty())
    }

    companion object {
        private const val OLD_LANDLORD_WITH_NO_PROPERTY_ID = 1L
        private const val BOUNDARY_LANDLORD_WITH_NO_PROPERTY_ID = 2L
        private const val ALREADY_INACTIVE_LANDLORD_ID = 3L
        private const val LANDLORD_WITH_ONLY_INACTIVE_PROPERTY_ID = 4L
        private const val LANDLORD_WITH_ACTIVE_PROPERTY_ID = 5L
        private const val LEGACY_LANDLORD_WITH_ACTIVE_PROPERTY_ID = 6L
    }
}
