package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class SelectAddressFormPageUpdateLandlordDetails(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${LandlordDetailsController.UPDATE_ROUTE}/${SelectAddressStep.ROUTE_SEGMENT}",
    )
