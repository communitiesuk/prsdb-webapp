package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.RemoveOrgUserConfirmationStep

class RemoveOrgUserConfirmationPage(
    page: Page,
) : BasePage(page, "/${RemoveOrgUserConfirmationStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1.govuk-heading-l"))

    val memberNameParagraph: Locator = page.locator("main p.govuk-body").first()

    val form = PostForm(page)

    val backLink = BackLink.default(page)

    val cancelLink = Link.byText(page, "Cancel and go back")
}
