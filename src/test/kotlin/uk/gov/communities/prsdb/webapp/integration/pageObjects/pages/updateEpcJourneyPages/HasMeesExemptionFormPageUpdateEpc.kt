package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasMeesExemptionFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep

class HasMeesExemptionFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : HasMeesExemptionFormBasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasMeesExemptionStep.ROUTE_SEGMENT}",
    )
