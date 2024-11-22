package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class EmailFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        urlSegment = "/register-as-a-landlord/email",
        pageHeading = "What is your email address?",
        inputLabel = "emailAddress",
    )
