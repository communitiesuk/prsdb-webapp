package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LA_USERS_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserAccessLevelDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository,
    val oneLoginUserService: OneLoginUserService,
) {
    fun getUserAndLocalAuthorityIfAuthorizedUser(
        localAuthorityId: Int,
        subjectId: String,
    ): Pair<LocalAuthorityUserDataModel, LocalAuthority> {
        val localAuthorityUser =
            localAuthorityUserRepository.findByBaseUser_Id(subjectId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $subjectId is not an LA user")
        val userModel =
            LocalAuthorityUserDataModel(
                localAuthorityUser.id,
                localAuthorityUser.name,
                localAuthorityUser.localAuthority.name,
                localAuthorityUser.isManager,
            )

        if (localAuthorityUser.localAuthority.id != localAuthorityId) {
            throw AccessDeniedException(
                "Local authority user for LA ${localAuthorityUser.localAuthority.id} tried to manage users for LA $localAuthorityId",
            )
        }

        return Pair(userModel, localAuthorityUser.localAuthority)
    }

    fun getLocalAuthorityUserIfAuthorizedLA(
        localAuthorityUserId: Long,
        localAuthorityId: Int,
    ): LocalAuthorityUserDataModel {
        val localAuthorityUser =
            localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localAuthorityUserId not found")

        if (localAuthorityUser.localAuthority.id != localAuthorityId) {
            throw AccessDeniedException("Local authority user $localAuthorityUserId does not belong to LA $localAuthorityId")
        }

        return LocalAuthorityUserDataModel(
            localAuthorityUserId,
            localAuthorityUser.name,
            localAuthorityUser.localAuthority.name,
            localAuthorityUser.isManager,
        )
    }

    fun getPaginatedUsersAndInvitations(
        localAuthority: LocalAuthority,
        currentPageNumber: Int,
        pageSize: Int = MAX_ENTRIES_IN_LA_USERS_TABLE_PAGE,
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

    fun getLocalAuthorityUserOrNull(localAuthorityUserId: Long) = localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)

    fun updateUserAccessLevel(
        localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelDataModel,
        localAuthorityUserId: Long,
    ) {
        val localAuthorityUser =
            localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localAuthorityUserId does not exist")

        localAuthorityUser.isManager = localAuthorityUserAccessLevel.isManager
        localAuthorityUserRepository.save(localAuthorityUser)
    }

    fun deleteUser(localAuthorityUserId: Long) {
        localAuthorityUserRepository.deleteById(localAuthorityUserId)
    }

    @Transactional
    fun registerUserAndReturnID(
        baseUserId: String,
        localAuthority: LocalAuthority,
        name: String,
        email: String,
    ): Long {
        val localAuthorityUser =
            localAuthorityUserRepository.save(
                LocalAuthorityUser(
                    baseUser = oneLoginUserService.findOrCreate1LUser(baseUserId),
                    isManager = false,
                    localAuthority = localAuthority,
                    name = name,
                    email = email,
                ),
            )

        return localAuthorityUser.id
    }

    fun getIsLocalAuthorityUser(baseUserId: String): Boolean = localAuthorityUserRepository.findByBaseUser_Id(baseUserId) != null
}
