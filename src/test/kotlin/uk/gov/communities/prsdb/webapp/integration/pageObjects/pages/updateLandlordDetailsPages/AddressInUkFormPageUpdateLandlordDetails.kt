package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep

class AddressInUkFormPageUpdateLandlordDetails(
    page: Page,
) : PageWithYesNoRadios(page, "${UpdateLandlordAddressController.UPDATE_ADDRESS_ROUTE}/${AddressInUkStep.ROUTE_SEGMENT}")
