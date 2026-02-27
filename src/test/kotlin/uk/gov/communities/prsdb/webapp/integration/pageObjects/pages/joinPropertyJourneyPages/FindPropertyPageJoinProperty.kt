package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class FindPropertyPageJoinProperty(
    page: Page,
) : BasePage(page, "$JOIN_PROPERTY_ROUTE/${LookupAddressStep.ROUTE_SEGMENT}") {
    val form = FindPropertyFormJoinProperty(page)
    val backLink = BackLink.default(page)

    class FindPropertyFormJoinProperty(
        page: Page,
    ) : Form(page) {
        val postcodeInput = TextInput.textByFieldName(locator, "postcode")
        val houseNameOrNumberInput = TextInput.textByFieldName(locator, "houseNameOrNumber")
        val submitButton = SubmitButton(locator)
    }
}
