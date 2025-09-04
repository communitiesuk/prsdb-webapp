package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LA_USER_ID
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LA_USERS_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityUserDeletionAdminEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityUserDeletionEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilRegistrationConfirmationEmail

@PrsdbWebService
class LocalAuthorityDataService(
    private val localAuthorityUserRepository: LocalAuthorityUserRepository,
    private val localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository,
    private val oneLoginUserService: OneLoginUserService,
    private val session: HttpSession,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val registrationConfirmationSender: EmailNotificationService<LocalCouncilRegistrationConfirmationEmail>,
    private val deletionConfirmationSender: EmailNotificationService<LocalAuthorityUserDeletionEmail>,
    private val deletionConfirmationSenderAdmin: EmailNotificationService<LocalAuthorityUserDeletionAdminEmail>,
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
                localAuthorityUser.email,
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
            localAuthorityUser.email,
        )
    }

    fun getPaginatedUsersAndInvitations(
        localAuthority: LocalAuthority,
        currentPageNumber: Int,
        pageSize: Int = MAX_ENTRIES_IN_LA_USERS_TABLE_PAGE,
        filterOutLaAdminInvitations: Boolean = true,
    ): Page<LocalAuthorityUserOrInvitationDataModel> {
        val pageRequest =
            PageRequest.of(
                currentPageNumber,
                pageSize,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        if (filterOutLaAdminInvitations) {
            return localAuthorityUserOrInvitationRepository
                .findByLocalAuthorityNotIncludingAdminInvitations(
                    localAuthority,
                    pageRequest,
                ).map {
                    LocalAuthorityUserOrInvitationDataModel(
                        id = it.id,
                        userNameOrEmail = it.name,
                        localAuthorityName = localAuthority.name,
                        isManager = it.isManager,
                        isPending = it.entityType == "local_authority_invitation",
                    )
                }
        }
        return localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest).map {
            LocalAuthorityUserOrInvitationDataModel(
                id = it.id,
                userNameOrEmail = it.name,
                localAuthorityName = localAuthority.name,
                isManager = it.isManager,
                isPending = it.entityType == "local_authority_invitation",
            )
        }
    }

    fun getLocalAuthorityUserOrNull(localAuthorityUserId: Long) = localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)

    fun updateUserAccessLevel(
        localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelRequestModel,
        localAuthorityUserId: Long,
    ) {
        val localAuthorityUser =
            localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localAuthorityUserId does not exist")

        localAuthorityUser.isManager = localAuthorityUserAccessLevel.isManager
        localAuthorityUserRepository.save(localAuthorityUser)
    }

    fun deleteUser(localAuthorityUserId: Long) {
        val localAuthorityUser =
            localAuthorityUserRepository.findByIdOrNull(localAuthorityUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localAuthorityUserId does not exist")

        val localAdminsByAuthority =
            localAuthorityUserRepository.findAllByLocalAuthority_IdAndIsManagerTrue(localAuthorityUser.localAuthority.id)

        for (admin in localAdminsByAuthority) {
            deletionConfirmationSenderAdmin.sendEmail(
                admin.email,
                LocalAuthorityUserDeletionAdminEmail(
                    councilName = localAuthorityUser.localAuthority.name,
                    email = localAuthorityUser.email,
                    userName = localAuthorityUser.name,
                    prsdURL = absoluteUrlProvider.buildLocalAuthorityDashboardUri().toString(),
                ),
            )
        }

        deletionConfirmationSender.sendEmail(
            localAuthorityUser.email,
            LocalAuthorityUserDeletionEmail(
                councilName = localAuthorityUser.localAuthority.name,
            ),
        )
        localAuthorityUserRepository.deleteById(localAuthorityUserId)
    }

    @Transactional
    fun registerUserAndReturnID(
        baseUserId: String,
        localAuthority: LocalAuthority,
        name: String,
        email: String,
        invitedAsAdmin: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
    ): Long {
        val localAuthorityUser =
            localAuthorityUserRepository.save(
                LocalAuthorityUser(
                    baseUser = oneLoginUserService.findOrCreate1LUser(baseUserId),
                    isManager = invitedAsAdmin,
                    localAuthority = localAuthority,
                    name = name,
                    email = email,
                    hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice,
                ),
            )

        registrationConfirmationSender.sendEmail(
            localAuthorityUser.email,
            LocalCouncilRegistrationConfirmationEmail(
                councilName = localAuthority.name,
                prsdURL = absoluteUrlProvider.buildLocalAuthorityDashboardUri().toString(),
                isAdmin = invitedAsAdmin,
            ),
        )

        return localAuthorityUser.id
    }

    fun getIsLocalAuthorityUser(baseUserId: String): Boolean = localAuthorityUserRepository.findByBaseUser_Id(baseUserId) != null

    fun getLocalAuthorityUser(baseUserId: String): LocalAuthorityUser {
        val localAuthorityUser =
            localAuthorityUserRepository.findByBaseUser_Id(baseUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $baseUserId not found")

        return localAuthorityUser
    }

    fun setLastUserIdRegisteredThisSession(localAuthorityUserId: Long) = session.setAttribute(LA_USER_ID, localAuthorityUserId)

    fun getLastUserIdRegisteredThisSession() = session.getAttribute(LA_USER_ID)?.toString()?.toLong()
}
