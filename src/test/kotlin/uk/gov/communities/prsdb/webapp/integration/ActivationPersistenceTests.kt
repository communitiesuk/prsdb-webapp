package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.services.ActivationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.LocalDate

class ActivationPersistenceTests : IntegrationTestWithMutableData(
    listOf(
        "data-mockuser-landlord-with-sole-and-joint-properties.sql",
        "data-overlay-joint-property-and-co-owner-are-inactive.sql",
    ),
) {
    @Autowired
    private lateinit var activationService: ActivationService

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `reactivating a property reactivates its inactive landlords`() {
        claimAndReactivate()

        assertTrue(propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get().isActive)
        val reactivatedLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()
        assertTrue(reactivatedLandlord.isActive)
        assertNull(reactivatedLandlord.lastDeactivatedDate)
    }

    @Test
    fun `reactivating a property leaves its already active landlords unchanged`() {
        val activeLandlordBefore = landlordRepository.findById(ACTIVE_LANDLORD_ID).get()

        claimAndReactivate()

        val activeLandlordAfter = landlordRepository.findById(ACTIVE_LANDLORD_ID).get()
        assertTrue(activeLandlordAfter.isActive)
        assertNull(activeLandlordAfter.lastDeactivatedDate)
        assertEquals(activeLandlordBefore.lastModifiedDate, activeLandlordAfter.lastModifiedDate)
    }

    @Test
    fun `reactivating a claimed property persists the renewal date and paid period it is given`() {
        val renewalDate = LocalDate.now().plusYears(2)

        claimAndReactivate(renewalDate, paidForUntil = renewalDate)

        val reactivatedProperty = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get()
        assertEquals(renewalDate, reactivatedProperty.renewalDate)
        assertEquals(renewalDate, reactivatedProperty.paidForUntil)
    }

    @Test
    fun `claiming an active property throws and leaves it unchanged`() {
        jdbcTemplate.update("UPDATE property_ownership SET is_active = true WHERE id = ?", JOINT_PROPERTY_ID)
        val propertyBefore = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get()

        assertThrows<PropertyAlreadyReactivatedException> { claimAndReactivate() }

        val propertyAfter = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get()
        assertEquals(propertyBefore.lastDeactivatedDate, propertyAfter.lastDeactivatedDate)
        assertEquals(propertyBefore.renewalDate, propertyAfter.renewalDate)
    }

    @Test
    fun `adding an inactive landlord to an active property reactivates the landlord`() {
        val inactiveLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()

        propertyOwnershipService.addLandlordToPropertyOwnership(ACTIVE_PROPERTY_ID, inactiveLandlord)

        val reactivatedLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()
        assertTrue(reactivatedLandlord.isActive)
        assertNull(reactivatedLandlord.lastDeactivatedDate)
    }

    private fun claimAndReactivate(
        renewalDate: LocalDate = LocalDate.now().plusYears(1),
        paidForUntil: LocalDate? = null,
    ) = TransactionTemplate(transactionManager).executeWithoutResult {
        val claimedProperty = activationService.claimPropertyForReactivation(JOINT_PROPERTY_ID)
        activationService.reactivateClaimedProperty(claimedProperty, renewalDate, paidForUntil)
    }

    companion object {
        private const val ACTIVE_PROPERTY_ID = 1L
        private const val JOINT_PROPERTY_ID = 2L
        private const val ACTIVE_LANDLORD_ID = 1L
        private const val INACTIVE_LANDLORD_ID = 2L
    }
}
