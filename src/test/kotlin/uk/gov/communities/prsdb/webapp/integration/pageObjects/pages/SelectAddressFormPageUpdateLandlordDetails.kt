package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class SelectAddressFormPageUpdateLandlordDetails(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${UpdateLandlordAddressController.UPDATE_ADDRESS_ROUTE}/${SelectAddressStep.ROUTE_SEGMENT}",
    ) {
    val warning = Warning.default(page)
}
