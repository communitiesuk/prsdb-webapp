package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    val submitButton = page.locator("button[type=\"submit\"]")
    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")

    fun submit(): Page {
        submitButton.click()
        return page
    }
}
