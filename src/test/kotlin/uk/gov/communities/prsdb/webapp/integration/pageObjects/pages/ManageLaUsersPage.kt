package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ManageLaUsersPage(
    page: Page,
) : BasePage(page, "/manage-users") {
    val table = Table(page)
    val inviteAnotherUserButton = Button.byText(page, "Invite another user")
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")

    companion object {
        const val USERNAME_COL_INDEX: Int = 0
        const val ACCESS_LEVEL_COL_INDEX: Int = 1
        const val ACCOUNT_STATUS_COL_INDEX: Int = 2
        const val ACTIONS_COL_INDEX: Int = 3
    }

    fun getChangeLink(rowIndex: Int) = table.getClickableCell(rowIndex, ACTIONS_COL_INDEX).link

    fun getPaginationComponent() = Pagination(page)
}
