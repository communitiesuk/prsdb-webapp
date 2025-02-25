package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage

class LookupAddressFormPageUpdateLandlordDetails(
    page: Page,
    // QQ - use constants
) : LookupAddressFormPage(page, "${LandlordDetailsController.UPDATE_ROUTE}/lookup-address")
