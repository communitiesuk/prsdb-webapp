package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep

class NoMatchingPropertiesPageJoinProperty(
    page: Page,
) : BasePage(page, "$JOIN_PROPERTY_ROUTE/${NoMatchingPropertiesStep.ROUTE_SEGMENT}") {
    val heading = page.locator("h1")
    val backLink = BackLink.default(page)
    val searchAgainLink = Link.byText(page.locator("html"), "search again")
    val findByPrnLink = Link.byText(page.locator("html"), "Property Registration Number")
    val form = NoMatchingPropertiesForm(page)

    class NoMatchingPropertiesForm(
        page: Page,
    ) : Form(page) {
        val submitButton = SubmitButton(locator)
    }
}
