package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class NameFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(page, pageHeading = "What is your full name?", inputLabel = "name")
