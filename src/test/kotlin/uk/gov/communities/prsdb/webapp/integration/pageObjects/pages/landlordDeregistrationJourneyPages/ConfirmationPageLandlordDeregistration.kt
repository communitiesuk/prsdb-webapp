package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLandlordDeregistration(
    page: Page,
) : BasePage(page, "${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT") {
    val confirmationBanner = ConfirmationBanner(page)
}
