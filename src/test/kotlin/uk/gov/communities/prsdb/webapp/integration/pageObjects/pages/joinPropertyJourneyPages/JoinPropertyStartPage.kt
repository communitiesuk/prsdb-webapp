package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class JoinPropertyStartPage(
    page: Page,
) : BasePage(page, JoinPropertyController.JOIN_PROPERTY_ROUTE) {
    val heading: Heading = Heading(page.locator("main h1"))
    val continueButton = Button.byText(page, "Continue")
    val insetText = page.locator(".govuk-inset-text")
    val detailsSummary = page.locator(".govuk-details__summary-text")
}
