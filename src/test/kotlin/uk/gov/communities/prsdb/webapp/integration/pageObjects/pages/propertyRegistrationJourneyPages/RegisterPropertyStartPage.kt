package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class RegisterPropertyStartPage(
    page: Page,
    // TODO PDJB-104 update urlSegment to full property registration journey route
) : BasePage(page, REGISTER_PROPERTY_JOURNEY_URL) {
    val heading: Locator? = page.locator(".govuk-heading-l")
    val startButton = Button.byText(page, "Start now")
}
