package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep

class NoAddressFoundFormPageUpdateLandlordDetails(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "$UPDATE_ROUTE/${NoAddressFoundStep.ROUTE_SEGMENT}",
    )
