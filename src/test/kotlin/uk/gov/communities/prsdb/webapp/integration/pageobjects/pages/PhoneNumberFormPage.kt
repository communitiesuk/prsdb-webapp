package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

class PhoneNumberFormPage(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertThat(fieldSetHeading).containsText("What is your phone number?")
    }
}
