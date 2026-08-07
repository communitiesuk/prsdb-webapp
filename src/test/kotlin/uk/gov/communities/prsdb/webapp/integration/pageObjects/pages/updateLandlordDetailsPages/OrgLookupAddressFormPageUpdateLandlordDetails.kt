package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class OrgLookupAddressFormPageUpdateLandlordDetails(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${UpdateOrganisationLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
