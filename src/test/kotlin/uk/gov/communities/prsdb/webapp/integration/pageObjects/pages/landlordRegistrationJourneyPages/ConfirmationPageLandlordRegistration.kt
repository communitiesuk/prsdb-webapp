package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$REGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT") {
    val confirmationBanner = BaseComponent.getConfirmationPageBanner(page)

    fun clickGoToDashboard() = clickButton(BaseComponent.getButton(page, "Go to Dashboard"))
}
