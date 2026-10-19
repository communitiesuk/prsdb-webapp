package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.RemoveOrgUserConfirmationStep

class RemoveOrgUserConfirmationPage(
    page: Page,
) : BasePage(page, "/${RemoveOrgUserConfirmationStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1.govuk-heading-xl"))

    val form = Form(page)

    val backLink = Link(page.locator(".govuk-back-link"))
}
