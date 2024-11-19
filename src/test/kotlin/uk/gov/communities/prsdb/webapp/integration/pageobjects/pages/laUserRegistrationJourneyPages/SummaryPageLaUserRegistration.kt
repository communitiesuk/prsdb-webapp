package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class SummaryPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    val bannerHeading = page.locator(".govuk-heading-l")
}
