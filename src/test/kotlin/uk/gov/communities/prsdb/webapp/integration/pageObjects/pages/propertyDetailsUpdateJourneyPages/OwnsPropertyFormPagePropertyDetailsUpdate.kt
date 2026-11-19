package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OwnsPropertyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep

class OwnsPropertyFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : OwnsPropertyFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${OwnsPropertyStep.ROUTE_SEGMENT}",
    )
