package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ManageLocalCouncilAdminsPage(
    page: Page,
) : BasePage(page, MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE) {
    val table = Table(page)
    val inviteAnotherAdminButton = Button.byText(page, "Invite another admin")
    val noAdminsHeader = Heading(page.locator("main h2.govuk-heading-m"))

    companion object {
        const val USERNAME_COL_INDEX: Int = 0
        const val LOCAL_COUNCIL_COL_INDEX: Int = 1
        const val ACCOUNT_STATUS_COL_INDEX: Int = 2
        const val ACTIONS_COL_INDEX: Int = 3
    }

    fun getChangeLink(rowIndex: Int) = table.getClickableCell(rowIndex, ACTIONS_COL_INDEX).link

    fun getPaginationComponent() = Pagination(page)
}
