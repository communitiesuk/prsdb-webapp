package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationalLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask

class OrgNonUkAddressFormPageUpdateLandlordDetails(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${UpdateOrganisationalLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE}/" +
            InternationalAddressTask.INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT,
    ) {
    val warning = Warning.default(page)
}
