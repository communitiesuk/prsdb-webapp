package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.NameFormBasePage

class NameFormPageLandlordRegistration(
    page: Page,
) : NameFormBasePage(page, pageHeading = "What is your full name?") {
    override fun submit(): EmailFormPageLandlordRegistration {
        submitButton.click()
        return createValid(page, EmailFormPageLandlordRegistration::class)
    }
}
