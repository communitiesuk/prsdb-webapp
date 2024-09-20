package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LandlordUserRepository

@Service
class UserRolesService(
    val landlordRepository: LandlordUserRepository,
) {
    fun getRolesforSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()
        val matchingLandlordUser = landlordRepository.findByBaseUser_Id(subjectId)
        if (matchingLandlordUser.isNotEmpty()) {
            roles.add("ROLE_LANDLORD")
        }
        return roles
    }
}
