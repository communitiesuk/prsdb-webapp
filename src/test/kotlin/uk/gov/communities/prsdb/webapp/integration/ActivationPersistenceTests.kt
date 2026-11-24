package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.services.ActivationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

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

    @Test
    fun `reactivating a property reactivates its inactive landlords`() {
        activationService.reactivateProperty(JOINT_PROPERTY_ID, landlordRepository.findById(ACTIVE_LANDLORD_ID).get())

        assertTrue(propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get().isActive)
        val reactivatedLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()
        assertTrue(reactivatedLandlord.isActive)
        assertNull(reactivatedLandlord.lastDeactivatedDate)
    }

    @Test
    fun `reactivating a property leaves its already active landlords unchanged`() {
        val activeLandlordBefore = landlordRepository.findById(ACTIVE_LANDLORD_ID).get()

        activationService.reactivateProperty(JOINT_PROPERTY_ID, landlordRepository.findById(ACTIVE_LANDLORD_ID).get())

        val activeLandlordAfter = landlordRepository.findById(ACTIVE_LANDLORD_ID).get()
        assertTrue(activeLandlordAfter.isActive)
        assertNull(activeLandlordAfter.lastDeactivatedDate)
        assertEquals(activeLandlordBefore.lastModifiedDate, activeLandlordAfter.lastModifiedDate)
    }

    @Test
    fun `reactivating a property persists the renewal date moved to the reactivating landlord's anniversary`() {
        val reactivatingLandlord = landlordRepository.findById(ACTIVE_LANDLORD_ID).get()

        activationService.reactivateProperty(JOINT_PROPERTY_ID, reactivatingLandlord)

        assertEquals(
            RenewalDateHelper.getRenewalDate(checkNotNull(reactivatingLandlord.anniversary)),
            propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get().renewalDate,
        )
    }

    @Test
    fun `adding an inactive landlord to an active property reactivates the landlord`() {
        val inactiveLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()

        propertyOwnershipService.addLandlordToPropertyOwnership(ACTIVE_PROPERTY_ID, inactiveLandlord)

        val reactivatedLandlord = landlordRepository.findById(INACTIVE_LANDLORD_ID).get()
        assertTrue(reactivatedLandlord.isActive)
        assertNull(reactivatedLandlord.lastDeactivatedDate)
    }

    companion object {
        private const val ACTIVE_PROPERTY_ID = 1L
        private const val JOINT_PROPERTY_ID = 2L
        private const val ACTIVE_LANDLORD_ID = 1L
        private const val INACTIVE_LANDLORD_ID = 2L
    }
}
