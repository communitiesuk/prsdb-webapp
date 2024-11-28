package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class RegisterPropertyStartPage(
    page: Page,
) : BasePage(page, "/$REGISTER_PROPERTY_JOURNEY_URL") {
    val heading: Locator? = page.locator(".govuk-heading-l")
    val startButton = getButton(page, "Start now")
}
