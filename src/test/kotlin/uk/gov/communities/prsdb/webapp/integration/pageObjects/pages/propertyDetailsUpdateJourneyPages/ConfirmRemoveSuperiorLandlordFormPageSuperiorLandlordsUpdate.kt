package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveSuperiorLandlordStep

class ConfirmRemoveSuperiorLandlordFormPageSuperiorLandlordsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ConfirmRemoveSuperiorLandlordStep.ROUTE_SEGMENT}",
    )
