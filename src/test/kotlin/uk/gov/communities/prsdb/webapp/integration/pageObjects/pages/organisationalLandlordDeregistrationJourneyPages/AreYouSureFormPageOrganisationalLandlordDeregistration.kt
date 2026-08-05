package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

// TODO: PDJB-1482 - Once the real "are you sure" page is built, extend AreYouSureFormBasePage instead of BasePage
class AreYouSureFormPageOrganisationalLandlordDeregistration(
    page: Page,
) : BasePage(page, DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_PATH)
