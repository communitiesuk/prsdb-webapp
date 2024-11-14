package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class EmailFormPageLaUserRegistration(
    page: Page,
) : FormBasePage(page, pageHeading = "What is your work email address?", inputLabel = "emailAddress")
