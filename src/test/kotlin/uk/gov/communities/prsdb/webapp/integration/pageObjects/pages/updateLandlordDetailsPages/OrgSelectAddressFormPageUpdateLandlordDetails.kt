package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationalLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class OrgSelectAddressFormPageUpdateLandlordDetails(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${UpdateOrganisationalLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE}/${SelectAddressStep.ROUTE_SEGMENT}",
    ) {
    val warning = Warning.default(page)
}
