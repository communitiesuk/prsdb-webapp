package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class PhoneNumberFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        urlSegment = "/register-as-a-landlord/phone-number",
        pageHeading = "What is your phone number?",
        inputLabel = "phoneNumber",
    )
