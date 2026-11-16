package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep

class RemovePropertyManagerFormPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RemovePropertyManagerStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val cancelLink = Link.byText(page, "Cancel")
}
