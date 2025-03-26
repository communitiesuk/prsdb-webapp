package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@Service
class LandlordDeregistrationService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val localAuthorityUserRepository: LocalAuthorityUserRepository,
) {
    @Transactional
    fun deregisterLandlord(baseUserId: String) {
        landlordRepository.deleteByBaseUser_Id(baseUserId)
        deleteFromOneLoginIfNotAnotherTypeOfUser(baseUserId)
    }

    fun deleteFromOneLoginIfNotAnotherTypeOfUser(baseUserId: String) {
        val userIsLocalAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(baseUserId) != null
        if (!userIsLocalAuthorityUser) {
            oneLoginUserRepository.deleteById(baseUserId)
        }
    }
}
