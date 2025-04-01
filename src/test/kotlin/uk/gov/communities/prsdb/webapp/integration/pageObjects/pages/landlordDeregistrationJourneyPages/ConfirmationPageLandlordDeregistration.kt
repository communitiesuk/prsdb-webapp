package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordDeregistration(
    page: Page,
) : BasePage(page, "$DEREGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT") {
    val confirmationBanner = ConfirmationBanner(page)
}
