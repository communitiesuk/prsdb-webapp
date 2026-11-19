package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FreeholderEmailFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep

class FreeholderEmailFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : FreeholderEmailFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${FreeholderEmailStep.ROUTE_SEGMENT}",
    )
