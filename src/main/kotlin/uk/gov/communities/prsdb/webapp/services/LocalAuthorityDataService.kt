package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserAccessLevelDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository,
) {
    fun getLocalAuthorityIfAuthorizedUser(
        localAuthorityId: Int,
        subjectId: String,
    ): LocalAuthority {
        val localAuthority =
            localAuthorityUserRepository.findByBaseUser_Id(subjectId)?.localAuthority
                ?: throw AccessDeniedException("User $subjectId is not an LA user")

        if (localAuthority.id != localAuthorityId) {
            throw AccessDeniedException(
                "Local authority user for LA ${localAuthority.id} tried to manage users for LA $localAuthorityId",
            )
        }

        return localAuthority
    }

    fun getLocalAuthorityUserIfAuthorizedUser(
        localAuthorityUserId: Long,
        localAuthorityId: Int,
        subjectId: String,
    ): LocalAuthorityUserDataModel {
        getLocalAuthorityIfAuthorizedUser(localAuthorityId, subjectId)

        val localAuthorityUser = localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)

        if (localAuthorityUser?.localAuthority?.id != localAuthorityId) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Local authority user $localAuthorityUserId does not exist for LA $localAuthorityId",
            )
        }

        return LocalAuthorityUserDataModel(
            localAuthorityUserId,
            localAuthorityUser.baseUser.name,
            localAuthorityUser.localAuthority.name,
            localAuthorityUser.isManager,
        )
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
                id = it.id,
                userName = it.name,
                localAuthorityName = localAuthority.name,
                isManager = it.isManager,
                isPending = it.entityType == "local_authority_invitation",
            )
        }
    }

    fun updateUserAccessLevel(
        localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelDataModel,
        localAuthorityUserId: Long,
        localAuthorityId: Int,
        subjectId: String,
    ) {
        getLocalAuthorityUserIfAuthorizedUser(localAuthorityUserId, localAuthorityId, subjectId)

        val localAuthorityUser =
            localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)
                ?: throw AccessDeniedException("User $localAuthorityUserId does not exist for LA $localAuthorityId")

        localAuthorityUser.isManager = localAuthorityUserAccessLevel.isManager
        localAuthorityUserRepository.save(localAuthorityUser)
    }
}
