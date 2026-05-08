package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionReasonBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep

class MeesExemptionFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : MeesExemptionReasonBasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${MeesExemptionStep.ROUTE_SEGMENT}",
    )
