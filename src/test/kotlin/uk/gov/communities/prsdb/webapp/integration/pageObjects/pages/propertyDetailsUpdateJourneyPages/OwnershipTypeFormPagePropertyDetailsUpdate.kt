package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OwnershipTypeFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep

class OwnershipTypeFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : OwnershipTypeFormPage(
        page,
        UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +

            "/${OwnershipTypeStep.ROUTE_SEGMENT}",
    )
