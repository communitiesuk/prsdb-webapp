package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.PhoneNumberBasePage

class PhoneNumberFormPageLandlordRegistration(
    page: Page,
) : PhoneNumberBasePage(page, pageHeading = "What is your phone number?") {
    override fun submit(): EmailFormPageLandlordRegistration {
        submitButton.click()
        return createValid(page, EmailFormPageLandlordRegistration::class)
    }
}
