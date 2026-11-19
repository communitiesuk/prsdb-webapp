package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordEmailStep

class SuperiorLandlordEmailFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : EmailFormPage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${SuperiorLandlordEmailStep.ROUTE_SEGMENT}",
    )
