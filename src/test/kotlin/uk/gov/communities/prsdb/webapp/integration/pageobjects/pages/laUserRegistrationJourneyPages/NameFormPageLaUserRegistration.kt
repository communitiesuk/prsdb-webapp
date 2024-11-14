package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class NameFormPageLaUserRegistration(
    page: Page,
) : FormBasePage(page, pageHeading = "What is your full name?", inputLabel = "name")
