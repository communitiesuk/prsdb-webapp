package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class LookupAddressFormPageUpdateLandlordDetails(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${UpdateLandlordAddressController.UPDATE_ADDRESS_ROUTE}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
