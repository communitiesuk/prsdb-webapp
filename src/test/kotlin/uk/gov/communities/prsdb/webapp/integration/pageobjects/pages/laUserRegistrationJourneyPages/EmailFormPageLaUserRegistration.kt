package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class EmailFormPageLaUserRegistration(
    page: Page,
) : FormBasePage(
        page,
        urlSegment = "/register-local-authority-user/email",
        pageHeading = "What is your work email address?",
        inputLabel = "emailAddress",
    )
