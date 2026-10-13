package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class TeamMembersPage(
    page: Page,
) : BasePage(page, TeamMembersController.TEAM_MEMBERS_ROUTE) {
    val heading = Heading(page.locator("main h1"))
    val successBanner = NotificationBanner(page)
    val inviteTeamMemberButton = Button.byText(page, "Invite a team member")
    val tabs = TeamMembersTabs(page)
    val administratorsSummaryList = TeamMembersSummaryList(page.locator("#${TeamMembersController.ADMINISTRATORS_FRAGMENT}"))
    val editorsSummaryList = TeamMembersSummaryList(page.locator("#${TeamMembersController.EDITORS_FRAGMENT}"))

    class TeamMembersTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToEditors(editorCount: Int) = goToTab("Editors ($editorCount)")
    }

    class TeamMembersSummaryList(
        parentLocator: Locator,
    ) : SummaryList(parentLocator) {
        fun getRowByIndex(index: Int) = getRow(index)
    }
}
