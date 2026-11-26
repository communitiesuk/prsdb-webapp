package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationalLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep

class OrgAddressInUkFormPageUpdateLandlordDetails(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "${UpdateOrganisationalLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE}/${AddressInUkStep.ROUTE_SEGMENT}",
    )
