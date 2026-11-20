package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep

class FreeholderNameFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : TextFormPage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${FreeholderNameStep.ROUTE_SEGMENT}",
    )
