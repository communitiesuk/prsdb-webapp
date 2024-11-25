package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getChildComponent
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class ManageLaUsersPage(
    page: Page,
) : BasePage(page, "Manage Local Authority Users") {
    val table = Table(page)
    val pagination by lazy { Pagination(page, this::class) }
    private val inviteAnotherUserButton = getButton(page, "Invite another user")
    val returnToDashboardButton: Locator = getButton(page, "Return to dashboard")

    companion object {
        const val USERNAME_COL_INDEX: Int = 0
        const val ACCESS_LEVEL_COL_INDEX: Int = 1
        const val ACCOUNT_STATUS_COL_INDEX: Int = 2
        const val ACTIONS_COL_INDEX: Int = 3
    }

    fun clickChangeLinkAndAssertNextPage(rowIndex: Int): EditLaUserPage {
        val changeLink = getChildComponent(table.getCell(rowIndex, ACTIONS_COL_INDEX), "a")
        return clickElementAndAssertNextPage(changeLink)
    }

    fun clickInviteAnotherUserAndAssertNextPage(): InviteNewLaUserPage = clickElementAndAssertNextPage(inviteAnotherUserButton)
}
