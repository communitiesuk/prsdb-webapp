package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getChildComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class ManageLaUsersPage(
    page: Page,
) : BasePage(page, "/manage-users") {
    val table = Table(page)
    val pagination = Pagination(page)
    val inviteAnotherUserButton = getButton(page, "Invite another user")
    val returnToDashboardButton: Locator = getButton(page, "Return to dashboard")

    companion object {
        const val USERNAME_COL_INDEX: Int = 0
        const val ACCESS_LEVEL_COL_INDEX: Int = 1
        const val ACCOUNT_STATUS_COL_INDEX: Int = 2
        const val ACTIONS_COL_INDEX: Int = 3
    }

    fun getChangeLink(rowIndex: Int) = getChildComponent(table.getCell(rowIndex, ACTIONS_COL_INDEX), "a")
}
