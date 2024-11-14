package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.NameFormBasePage

class NameFormPageLaUserRegistration(
    page: Page,
) : NameFormBasePage(page, pageHeading = "What is your full name?") {
    override fun submit(): EmailFormPageLaUserRegistration {
        submitButton.click()
        return createValid(page, EmailFormPageLaUserRegistration::class)
    }
}
