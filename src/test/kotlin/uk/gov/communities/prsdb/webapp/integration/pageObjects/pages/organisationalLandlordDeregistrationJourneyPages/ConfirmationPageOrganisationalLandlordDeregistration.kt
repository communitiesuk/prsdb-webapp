package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageOrganisationalLandlordDeregistration(
    page: Page,
) : BasePage(
        page,
        "${DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
}
