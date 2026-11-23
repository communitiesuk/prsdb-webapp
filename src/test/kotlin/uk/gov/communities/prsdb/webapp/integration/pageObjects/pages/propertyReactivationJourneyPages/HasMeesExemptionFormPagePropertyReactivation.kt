package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasMeesExemptionFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep

class HasMeesExemptionFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : HasMeesExemptionFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HasMeesExemptionStep.ROUTE_SEGMENT}",
    )
