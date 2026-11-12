package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcExemptionFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep

class EpcExemptionFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcExemptionFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EpcExemptionStep.ROUTE_SEGMENT}",
    )
