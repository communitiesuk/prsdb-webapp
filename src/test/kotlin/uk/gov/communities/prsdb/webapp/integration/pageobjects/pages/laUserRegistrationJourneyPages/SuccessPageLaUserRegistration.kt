package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class SuccessPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    val bannerHeading = page.locator(".govuk-panel__title")

    val bodyHeading = page.locator(".govuk-heading-m")
}
