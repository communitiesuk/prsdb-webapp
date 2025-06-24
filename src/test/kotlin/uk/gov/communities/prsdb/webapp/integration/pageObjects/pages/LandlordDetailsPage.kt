package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class LandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
