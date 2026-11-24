package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository

class LandlordDeactivationPersistenceTests : IntegrationTestWithMutableData("data-landlords-for-deactivation-task.sql") {
    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Test
    fun `no active properties query only returns active landlords whose properties are all inactive`() {
        val ids = landlordRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties()

        assertEquals(listOf(LANDLORD_WITH_ONLY_INACTIVE_PROPERTY_ID), ids)
    }

    companion object {
        private const val LANDLORD_WITH_ONLY_INACTIVE_PROPERTY_ID = 4L
    }
}
