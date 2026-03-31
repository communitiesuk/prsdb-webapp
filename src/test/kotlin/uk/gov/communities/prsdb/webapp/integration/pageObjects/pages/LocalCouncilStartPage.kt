package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilStartPageController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LocalCouncilStartPage(
    page: Page,
) : BasePage(page, LocalCouncilStartPageController.LOCAL_COUNCIL_START_PAGE_ROUTE) {
    val heading: Heading = Heading(page.locator("main h1"))
    val startButton = Button.byText(page, "Start now")
}
