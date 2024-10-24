package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import kotlin.math.ceil

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityInvitationRepository: LocalAuthorityInvitationRepository,
) {
    fun getLocalAuthorityUsersForLocalAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthority(localAuthority, pageRequest)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }

    fun getLocalAuthorityPendingUsersForLocalAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityUserDataModel> {
        val pendingUsers = localAuthorityInvitationRepository.findByInvitingAuthority(localAuthority, pageRequest)
        return pendingUsers.map { LocalAuthorityUserDataModel(it.invitedEmail, isManager = false, isPending = true) }
    }

    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }

    fun countActiveLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): Long =
        localAuthorityUserRepository.countByLocalAuthority(localAuthority)

    fun countPendingLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): Long =
        localAuthorityInvitationRepository.countByInvitingAuthority(localAuthority)

    fun getUserList(
        localAuthority: LocalAuthority,
        currentPageNumber: Int,
        nActiveUsers: Long,
        shouldPaginate: Boolean,
    ): List<LocalAuthorityUserDataModel> {
        var activeUsers = listOf<LocalAuthorityUserDataModel>()
        var pendingUsers = listOf<LocalAuthorityUserDataModel>()

        val firstDisplayedUserCombinedIndex = (currentPageNumber - 1) * MAX_ENTRIES_IN_TABLE_PAGE
        if (firstDisplayedUserCombinedIndex < nActiveUsers) {
            activeUsers = getActiveUsersPaginated(localAuthority, currentPageNumber, MAX_ENTRIES_IN_TABLE_PAGE)
        }

        if (activeUsers.size < MAX_ENTRIES_IN_TABLE_PAGE) {
            if (activeUsers.isNotEmpty()) {
                val nPendingUsersOnMixedPage = MAX_ENTRIES_IN_TABLE_PAGE - activeUsers.size
                pendingUsers = getPendingUsersPaginated(localAuthority, 1, nPendingUsersOnMixedPage, 0)
            } else {
                val nPagesWithActiveUsers = ceil(nActiveUsers.toDouble() / MAX_ENTRIES_IN_TABLE_PAGE.toDouble()).toInt()
                val pendingUserPageNumber = currentPageNumber - nPagesWithActiveUsers

                val nActiveUsersOnMixedPage = (nActiveUsers % MAX_ENTRIES_IN_TABLE_PAGE).toInt()
                val nPendingUsersOnMixedPage =
                    if (nActiveUsersOnMixedPage == 0) {
                        0
                    } else {
                        (MAX_ENTRIES_IN_TABLE_PAGE - nActiveUsersOnMixedPage)
                    }

                pendingUsers =
                    getPendingUsersPaginated(localAuthority, pendingUserPageNumber, MAX_ENTRIES_IN_TABLE_PAGE, nPendingUsersOnMixedPage)
            }
        }

        return activeUsers + pendingUsers
    }

    private fun getActiveUsersPaginated(
        localAuthority: LocalAuthority,
        page: Int,
        nUsers: Int,
    ): List<LocalAuthorityUserDataModel> {
        val pageRequest = PageRequest.of(page - 1, nUsers, Sort.by(Sort.Direction.ASC, "baseUser_name"))
        return getLocalAuthorityUsersForLocalAuthority(localAuthority, pageRequest)
    }

    private fun getPendingUsersPaginated(
        localAuthority: LocalAuthority,
        page: Int,
        nUsers: Int,
        initialOffset: Int,
    ): List<LocalAuthorityUserDataModel> {
        val pageRequest = PageRequestWithOffset(page - 1, nUsers, Sort.by(Sort.Direction.ASC, "invitedEmail"), initialOffset)
        return getLocalAuthorityPendingUsersForLocalAuthority(localAuthority, pageRequest)
    }
}

class PageRequestWithOffset(
    pageNumber: Int,
    pageSize: Int,
    sort: Sort,
    private val initialOffset: Int,
) : PageRequest(pageNumber, pageSize, sort) {
    override fun getOffset(): Long = (this.pageNumber * this.pageSize + initialOffset).toLong()
}
