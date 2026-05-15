package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordRegistration(
    page: Page,
) : BasePage(page, RegisterLandlordController.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE) {
    val confirmationBanner = LandlordRegistrationConfirmationBanner(page)
    val goToDashboardLink = Link.byText(page, "Go to dashboard")

    class LandlordRegistrationConfirmationBanner(
        page: Page,
    ) : ConfirmationBanner(page) {
        val registrationNumberText: String
            get() = locator.locator("strong").innerText()
    }
}
