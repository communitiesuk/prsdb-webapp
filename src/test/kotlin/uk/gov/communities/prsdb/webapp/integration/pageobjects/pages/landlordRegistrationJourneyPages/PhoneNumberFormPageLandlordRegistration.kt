package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class PhoneNumberFormPageLandlordRegistration(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertThat(fieldSetHeading).containsText("What is your phone number?")
    }
}
