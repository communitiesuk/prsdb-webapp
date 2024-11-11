package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PageNotFoundPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.EmailFormBasePage

class EmailFormPageLaUserRegistration(
    page: Page,
) : EmailFormBasePage(page, pageHeading = "What is your work email address?") {
    // When the "create answers" page is written, submitting this page should redirect to there
    override fun submit(): PageNotFoundPage {
        submitButton.click()
        return createValid(page, PageNotFoundPage::class)
    }
}
