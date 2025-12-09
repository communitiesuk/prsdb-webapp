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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_INVITATION_ENTITY_TYPE
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USER_ID
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LOCAL_COUNCIL_USERS_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilAdminUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserDeletionEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserDeletionInformAdminEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserInvitationInformAdminEmail

@PrsdbWebService
class LocalCouncilDataService(
    private val localCouncilUserRepository: LocalCouncilUserRepository,
    private val localCouncilUserOrInvitationRepository: LocalCouncilUserOrInvitationRepository,
    private val invitationService: LocalCouncilInvitationService,
    private val oneLoginUserService: OneLoginUserService,
    private val session: HttpSession,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val registrationConfirmationSender: EmailNotificationService<LocalCouncilRegistrationConfirmationEmail>,
    private val deletionConfirmationSender: EmailNotificationService<LocalCouncilUserDeletionEmail>,
    private val deletionConfirmationSenderAdmin: EmailNotificationService<LocalCouncilUserDeletionInformAdminEmail>,
    private val invitationConfirmationSenderAdmin: EmailNotificationService<LocalCouncilUserInvitationInformAdminEmail>,
) {
    fun getUserAndLocalCouncilIfAuthorizedUser(
        localCouncilId: Int,
        subjectId: String,
    ): Pair<LocalCouncilUserDataModel, LocalCouncil> {
        val localCouncilUser =
            localCouncilUserRepository.findByBaseUser_Id(subjectId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $subjectId is not a Local Council user")
        val userModel =
            LocalCouncilUserDataModel(
                localCouncilUser.id,
                localCouncilUser.name,
                localCouncilUser.localCouncil.name,
                localCouncilUser.isManager,
                localCouncilUser.email,
            )

        if (localCouncilUser.localCouncil.id != localCouncilId) {
            throw AccessDeniedException(
                "Local Council user for Local Council ${localCouncilUser.localCouncil.id}" +
                    " tried to manage users for Local Council $localCouncilId",
            )
        }

        return Pair(userModel, localCouncilUser.localCouncil)
    }

    fun getLocalCouncilUserIfAuthorizedLocalCouncil(
        localCouncilUserId: Long,
        localCouncilId: Int,
    ): LocalCouncilUser {
        val localCouncilUser =
            localCouncilUserRepository.findByIdOrNull(localCouncilUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localCouncilUserId not found")

        if (localCouncilUser.localCouncil.id != localCouncilId) {
            throw AccessDeniedException("Local Council user $localCouncilUserId does not belong to Local Council $localCouncilId")
        }

        return localCouncilUser
    }

    fun getPaginatedUsersAndInvitations(
        localCouncil: LocalCouncil,
        currentPageNumber: Int,
        pageSize: Int = MAX_ENTRIES_IN_LOCAL_COUNCIL_USERS_TABLE_PAGE,
        filterOutLocalCouncilAdminInvitations: Boolean = true,
    ): Page<LocalCouncilUserOrInvitationDataModel> {
        val pageRequest =
            PageRequest.of(
                currentPageNumber,
                pageSize,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        if (filterOutLocalCouncilAdminInvitations) {
            return localCouncilUserOrInvitationRepository
                .findByLocalCouncilNotIncludingAdminInvitations(
                    localCouncil,
                    pageRequest,
                ).map {
                    LocalCouncilUserOrInvitationDataModel(
                        id = it.id,
                        userNameOrEmail = it.name,
                        localCouncilName = localCouncil.name,
                        isManager = it.isManager,
                        isPending = it.entityType == LOCAL_COUNCIL_INVITATION_ENTITY_TYPE,
                    )
                }
        }

        return localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest).map {
            LocalCouncilUserOrInvitationDataModel(
                id = it.id,
                userNameOrEmail = it.name,
                localCouncilName = localCouncil.name,
                isManager = it.isManager,
                isPending = it.entityType == LOCAL_COUNCIL_INVITATION_ENTITY_TYPE,
            )
        }
    }

    fun getPaginatedAdminUsersAndInvitations(
        currentPageNumber: Int,
        pageSize: Int = MAX_ENTRIES_IN_LOCAL_COUNCIL_USERS_TABLE_PAGE,
    ): Page<LocalCouncilAdminUserOrInvitationDataModel> {
        val pageRequest =
            PageRequest.of(
                currentPageNumber,
                pageSize,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("localCouncil.name"), Sort.Order.asc("name")),
            )
        return localCouncilUserOrInvitationRepository.findAllByIsManagerTrue(pageRequest).map {
            LocalCouncilAdminUserOrInvitationDataModel(
                id = it.id,
                userNameOrEmail = it.name,
                localCouncilName = it.localCouncil.name,
                isPending = it.entityType == LOCAL_COUNCIL_INVITATION_ENTITY_TYPE,
            )
        }
    }

    fun getLocalCouncilUserOrNull(localCouncilUserId: Long) = localCouncilUserRepository.findByIdOrNull(localCouncilUserId)

    fun updateUserAccessLevel(
        localCouncilUserAccessLevel: LocalCouncilUserAccessLevelRequestModel,
        localCouncilUserId: Long,
    ) {
        val localCouncilUser =
            localCouncilUserRepository.findByIdOrNull(localCouncilUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $localCouncilUserId does not exist")

        localCouncilUser.isManager = localCouncilUserAccessLevel.isManager
        localCouncilUserRepository.save(localCouncilUser)
    }

    fun deleteUser(localCouncilUser: LocalCouncilUser) {
        localCouncilUserRepository.deleteById(localCouncilUser.id)

        deletionConfirmationSender.sendEmail(
            localCouncilUser.email,
            LocalCouncilUserDeletionEmail(
                councilName = localCouncilUser.localCouncil.name,
            ),
        )

        sendUserDeletedEmailsToAdmins(localCouncilUser)
    }

    fun sendUserInvitedEmailsToAdmins(
        localCouncil: LocalCouncil,
        invitedEmail: String,
    ) {
        val localAdminsByCouncil =
            localCouncilUserRepository.findAllByLocalCouncil_IdAndIsManagerTrue(localCouncil.id)

        val emailToAdmins =
            LocalCouncilUserInvitationInformAdminEmail(
                councilName = localCouncil.name,
                email = invitedEmail,
                prsdURL = absoluteUrlProvider.buildLocalCouncilDashboardUri().toString(),
            )

        for (admin in localAdminsByCouncil) {
            invitationConfirmationSenderAdmin.sendEmail(
                admin.email,
                emailToAdmins,
            )
        }
    }

    private fun sendUserDeletedEmailsToAdmins(localCouncilUser: LocalCouncilUser) {
        val localAdminsByCouncil =
            localCouncilUserRepository.findAllByLocalCouncil_IdAndIsManagerTrue(localCouncilUser.localCouncil.id)

        for (admin in localAdminsByCouncil) {
            deletionConfirmationSenderAdmin.sendEmail(
                admin.email,
                LocalCouncilUserDeletionInformAdminEmail(
                    councilName = localCouncilUser.localCouncil.name,
                    email = localCouncilUser.email,
                    userName = localCouncilUser.name,
                    prsdURL = absoluteUrlProvider.buildLocalCouncilDashboardUri().toString(),
                ),
            )
        }
    }

    @Transactional
    fun registerUserAndReturnID(
        baseUserId: String,
        localCouncil: LocalCouncil,
        name: String,
        email: String,
        invitedAsAdmin: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
    ): Long {
        val localCouncilUser =
            localCouncilUserRepository.save(
                LocalCouncilUser(
                    baseUser = oneLoginUserService.findOrCreate1LUser(baseUserId),
                    isManager = invitedAsAdmin,
                    localCouncil = localCouncil,
                    name = name,
                    email = email,
                    hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice,
                ),
            )

        registrationConfirmationSender.sendEmail(
            localCouncilUser.email,
            LocalCouncilRegistrationConfirmationEmail(
                councilName = localCouncil.name,
                prsdURL = absoluteUrlProvider.buildLocalCouncilDashboardUri().toString(),
                isAdmin = invitedAsAdmin,
            ),
        )

        return localCouncilUser.id
    }

    fun getIsLocalCouncilUser(baseUserId: String): Boolean = localCouncilUserRepository.findByBaseUser_Id(baseUserId) != null

    fun getLocalCouncilUser(baseUserId: String): LocalCouncilUser {
        val localCouncilUser =
            localCouncilUserRepository.findByBaseUser_Id(baseUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $baseUserId not found")

        return localCouncilUser
    }

    fun getLocalCouncilUserById(localCouncilUserId: Long): LocalCouncilUser =
        localCouncilUserRepository.findByIdOrNull(localCouncilUserId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Local Council users with ID $localCouncilUserId not found")

    fun setLastUserIdRegisteredThisSession(localCouncilUserId: Long) = session.setAttribute(LOCAL_COUNCIL_USER_ID, localCouncilUserId)

    fun getLastUserIdRegisteredThisSession() = session.getAttribute(LOCAL_COUNCIL_USER_ID)?.toString()?.toLong()

    fun getUsersDeletedThisSession(): MutableList<LocalCouncilUser> =
        session.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION) as MutableList<LocalCouncilUser>?
            ?: mutableListOf()

    fun getUserDeletedThisSessionById(localCouncilUserId: Long): LocalCouncilUser {
        val deletedUser =
            getUsersDeletedThisSession().find { it.id == localCouncilUserId }
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with id $localCouncilUserId was not found in the list of deleted users in the session",
                )

        throwErrorIfLocalCouncilUserExists(deletedUser)

        return deletedUser
    }

    private fun throwErrorIfLocalCouncilUserExists(localCouncilUser: LocalCouncilUser) {
        if (localCouncilUserRepository.existsById(localCouncilUser.id)) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "User with id ${localCouncilUser.id} is still in the local_council_user table",
            )
        }
    }

    fun addDeletedUserToSession(deletedUser: LocalCouncilUser) =
        session.setAttribute(
            LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION,
            getUsersDeletedThisSession().plus(deletedUser),
        )

    fun getInvitationsCancelledThisSession(): MutableList<LocalCouncilInvitation> =
        session.getAttribute(LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION) as MutableList<LocalCouncilInvitation>?
            ?: mutableListOf()

    fun getInvitationCancelledThisSessionById(invitationId: Long): LocalCouncilInvitation {
        val invitation =
            getInvitationsCancelledThisSession().find { it.id == invitationId }
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invitation with id $invitationId was not found in the list of cancelled invitations in the session",
                )

        invitationService.throwErrorIfInvitationExists(invitation)

        return invitation
    }

    fun addCancelledInvitationToSession(invitation: LocalCouncilInvitation) =
        session.setAttribute(
            LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION,
            getInvitationsCancelledThisSession().plus(invitation),
        )

    fun getLastLocalCouncilUserInvitedThisSession(localCouncilId: Int): String? =
        getLocalCouncilUsersInvitedThisSession().lastOrNull { it.first == localCouncilId }?.second

    private fun getLocalCouncilUsersInvitedThisSession(): MutableList<Pair<Int, String>> =
        session.getAttribute(LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION) as MutableList<Pair<Int, String>>?
            ?: mutableListOf()

    fun addInvitedLocalCouncilUserToSession(
        localCouncilId: Int,
        invitedEmail: String,
    ) = session.setAttribute(
        LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION,
        getLocalCouncilUsersInvitedThisSession().plus(Pair(localCouncilId, invitedEmail)),
    )
}
