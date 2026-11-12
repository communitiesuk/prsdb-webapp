package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.LocalDate

class ActiveInactiveServicePersistenceTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
    @Autowired
    private lateinit var activeInactiveService: ActiveInactiveService

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `deactivating a jointly owned property deactivates only the landlord with no other active property`() {
        activeInactiveService.deactivateProperty(JOINT_PROPERTY_ID)

        val property = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get()
        assertFalse(property.isActive)
        assertEquals(LocalDate.now(DateTimeHelper.UK_ZONE), property.lastDeactivatedDate)
        assertTrue(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)
        val deactivatedLandlord = landlordRepository.findById(LANDLORD_WITH_NO_OTHER_PROPERTY_ID).get()
        assertFalse(deactivatedLandlord.isActive)
        assertEquals(LocalDate.now(DateTimeHelper.UK_ZONE), deactivatedLandlord.lastDeactivatedDate)
    }

    @Test
    fun `deactivating a landlord's last active property deactivates the landlord`() {
        activeInactiveService.deactivateProperty(SOLE_PROPERTY_ID)
        assertTrue(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)

        activeInactiveService.deactivateProperty(JOINT_PROPERTY_ID)
        assertFalse(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)
    }

    @Test
    fun `adding an inactive landlord to an active property reactivates the landlord`() {
        activeInactiveService.deactivateProperty(JOINT_PROPERTY_ID)
        val inactiveLandlord = landlordRepository.findById(LANDLORD_WITH_NO_OTHER_PROPERTY_ID).get()
        assertFalse(inactiveLandlord.isActive)

        propertyOwnershipService.addLandlordToPropertyOwnership(SOLE_PROPERTY_ID, inactiveLandlord)

        val reactivatedLandlord = landlordRepository.findById(LANDLORD_WITH_NO_OTHER_PROPERTY_ID).get()
        assertTrue(reactivatedLandlord.isActive)
        assertNull(reactivatedLandlord.lastDeactivatedDate)
    }

    companion object {
        private const val SOLE_PROPERTY_ID = 1L
        private const val JOINT_PROPERTY_ID = 2L
        private const val LANDLORD_WITH_OTHER_PROPERTY_ID = 1L
        private const val LANDLORD_WITH_NO_OTHER_PROPERTY_ID = 2L
    }
}
