package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    val submitButton = page.locator("button[type=\"submit\"]")
    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")

    override fun validate() {
        assertThat(headingCaption).containsText("Before you register")
        assertThat(heading).containsText("Registering as a local authority user")
    }

    fun submit(): NameFormPageLaUserRegistration {
        submitButton.click()
        return createValid(page, NameFormPageLaUserRegistration::class)
    }

    fun assertHeadingContains(text: String) {
        assertThat(heading).containsText(text)
    }
}
