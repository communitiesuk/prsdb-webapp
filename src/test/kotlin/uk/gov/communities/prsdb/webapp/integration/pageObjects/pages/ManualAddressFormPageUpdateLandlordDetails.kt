package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep

class ManualAddressFormPageUpdateLandlordDetails(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${LandlordDetailsController.UPDATE_ROUTE}/${ManualAddressStep.ROUTE_SEGMENT}",
    )
