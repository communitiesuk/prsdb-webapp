package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FreeholdersInfoFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep

class FreeholdersInfoFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : FreeholdersInfoFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${FreeholdersInfoStep.ROUTE_SEGMENT}",
    )
