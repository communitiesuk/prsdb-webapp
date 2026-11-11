package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.services.ActiveStatusService
import java.time.LocalDate

class ActiveStatusServicePersistenceTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
    @Autowired
    private lateinit var activeStatusService: ActiveStatusService

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Test
    fun `deactivating a jointly owned property deactivates only the landlord with no other active property`() {
        activeStatusService.deactivateProperty(JOINT_PROPERTY_ID, DEACTIVATION_DATE)

        val property = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get()
        assertFalse(property.isActive)
        assertEquals(DEACTIVATION_DATE, property.lastDeactivatedDate)
        assertTrue(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)
        val deactivatedLandlord = landlordRepository.findById(LANDLORD_WITH_NO_OTHER_PROPERTY_ID).get()
        assertFalse(deactivatedLandlord.isActive)
        assertEquals(DEACTIVATION_DATE, deactivatedLandlord.lastDeactivatedDate)
    }

    @Test
    fun `deactivating a landlord's last active property deactivates the landlord`() {
        activeStatusService.deactivateProperty(SOLE_PROPERTY_ID, DEACTIVATION_DATE)
        assertTrue(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)

        activeStatusService.deactivateProperty(JOINT_PROPERTY_ID, DEACTIVATION_DATE)
        assertFalse(landlordRepository.findById(LANDLORD_WITH_OTHER_PROPERTY_ID).get().isActive)
    }

    companion object {
        private const val SOLE_PROPERTY_ID = 1L
        private const val JOINT_PROPERTY_ID = 2L
        private const val LANDLORD_WITH_OTHER_PROPERTY_ID = 1L
        private const val LANDLORD_WITH_NO_OTHER_PROPERTY_ID = 2L
        private val DEACTIVATION_DATE = LocalDate.of(2026, 11, 10)
    }
}
