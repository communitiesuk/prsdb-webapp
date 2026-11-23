package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionReasonBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep

class MeesExemptionFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : MeesExemptionReasonBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${MeesExemptionStep.ROUTE_SEGMENT}",
    )
