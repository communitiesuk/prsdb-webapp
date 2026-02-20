package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep

class FindPropertyPage(
    page: Page,
) : BasePage(page, "$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}") {
    val form = FindPropertyForm(page)
    val alternativeLink = Link(page.locator("a.govuk-link:has-text('Find using the Property Registration Number instead')"))
    val backLink = BackLink.default(page)

    class FindPropertyForm(
        page: Page,
    ) : Form(page) {
        val postcodeInput = TextInput.textByFieldName(locator, "postcode")
        val houseNameOrNumberInput = TextInput.textByFieldName(locator, "houseNameOrNumber")
        val submitButton = SubmitButton(locator)
    }
}
