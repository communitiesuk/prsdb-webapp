package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@Service
class LandlordDeregistrationService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
) {
    @Transactional
    fun deregisterLandlord(baseUserId: String) {
        landlordRepository.deleteByBaseUser_Id(baseUserId)
        oneLoginUserRepository.deleteIfNotLocalAuthorityUser(baseUserId)
    }

    @Transactional
    fun deregisterLandlordsProperties(baseUserId: String) {
        // retrieve all property ownerships

        // delete all licenses
        // delete all properties
        // delete all property ownerships
    }
}
