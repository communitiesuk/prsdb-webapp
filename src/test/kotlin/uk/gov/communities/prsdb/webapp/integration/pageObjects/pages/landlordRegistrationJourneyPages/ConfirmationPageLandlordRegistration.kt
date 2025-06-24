package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordRegistration(
    page: Page,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT") {
    val confirmationBanner = LandlordRegistrationConfirmationBanner(page)
    val goToDashboardButton = Button.byText(page, "Go to Dashboard")

    class LandlordRegistrationConfirmationBanner(
        page: Page,
    ) : ConfirmationBanner(page) {
        val registrationNumberText: String
            get() = locator.locator("strong").innerText()
    }
}
