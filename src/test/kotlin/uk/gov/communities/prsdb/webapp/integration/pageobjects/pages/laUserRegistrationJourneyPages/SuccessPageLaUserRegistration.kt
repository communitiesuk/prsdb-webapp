package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SuccessPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/success") {
    val bannerHeading = page.locator(".govuk-panel__title")

    val bodyHeading = page.locator(".govuk-heading-m")

    val errorHeading = page.locator(".govuk-heading-l")
}
