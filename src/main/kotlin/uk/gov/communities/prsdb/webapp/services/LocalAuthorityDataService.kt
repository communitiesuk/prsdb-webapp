package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository,
) {
    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }

    fun getPaginatedUsersAndInvitations(
        localAuthority: LocalAuthority,
        currentPageNumber: Int,
        pageSize: Int = MAX_ENTRIES_IN_TABLE_PAGE,
    ): Page<LocalAuthorityUserDataModel> {
        val pageRequest =
            PageRequest.of(
                currentPageNumber,
                pageSize,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        return localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest).map {
            LocalAuthorityUserDataModel(
                userName = it.name,
                isManager = it.isManager,
                isPending = it.entityType == "local_authority_invitation",
            )
        }
    }
}
