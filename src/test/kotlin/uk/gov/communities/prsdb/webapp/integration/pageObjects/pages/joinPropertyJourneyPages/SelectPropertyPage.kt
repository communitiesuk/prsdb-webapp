package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep

class SelectPropertyPage(
    page: Page,
) : BasePage(page, "$JOIN_PROPERTY_ROUTE/${SelectPropertyStep.ROUTE_SEGMENT}") {
    val heading: Heading = Heading(page.locator("main h1"))
    val hintText: Locator = page.locator("#selectedOption-hint")
    val searchAgainLink: Locator = page.locator("a:has-text('Search again')")
    val radioButtons: Locator = page.locator("input[type='radio'][name='selectedOption']")
    val detailsSummary: Locator = page.locator(".govuk-details__summary-text")
    val detailsText: Locator = page.locator(".govuk-details__text")
    val continueButton: Button = Button.byText(page, "Continue")
    val form: Form = Form(page.locator("form"))

    fun selectProperty(index: Int) {
        radioButtons.nth(index).click()
    }
}
