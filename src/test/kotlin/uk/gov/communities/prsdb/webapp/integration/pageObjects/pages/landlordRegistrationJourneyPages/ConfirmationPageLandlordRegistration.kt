package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$REGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT") {
    val confirmationBanner = LandlordRegistrationConfirmationBanner(page)
    val goToDashboardButton = Button.byText(page, "Go to Dashboard")

    class LandlordRegistrationConfirmationBanner(
        page: Page,
    ) : ConfirmationBanner(page) {
        val registrationNumberText: String
            get() = locator.locator("strong").innerText()
    }
}
