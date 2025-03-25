package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository

@Service
class LandlordDeregistrationService(
    private val landlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository,
) {
    fun getLandlordHasRegisteredProperties(baseUserId: String): Boolean {
        val landlordWithListedPropertyCount =
            landlordWithListedPropertyCountRepository.findByLandlord_BaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("Landlord with baseUserId $baseUserId not found")
        return landlordWithListedPropertyCount.listedPropertyCount > 0
    }
}
