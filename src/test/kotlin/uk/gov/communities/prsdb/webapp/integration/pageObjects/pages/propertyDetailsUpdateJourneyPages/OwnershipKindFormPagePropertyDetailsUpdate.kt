package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OwnershipKindFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep

class OwnershipKindFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : OwnershipKindFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${OwnershipKindStep.ROUTE_SEGMENT}",
    )
