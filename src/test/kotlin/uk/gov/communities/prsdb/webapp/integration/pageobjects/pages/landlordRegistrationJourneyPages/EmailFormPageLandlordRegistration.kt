package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.EmailFormBasePage

class EmailFormPageLandlordRegistration(
    page: Page,
) : EmailFormBasePage(page, pageHeading = "What is your email address?") {
    override fun submit(): PhoneNumberFormPageLandlordRegistration {
        submitButton.click()
        return createValid(page, PhoneNumberFormPageLandlordRegistration::class)
    }
}
