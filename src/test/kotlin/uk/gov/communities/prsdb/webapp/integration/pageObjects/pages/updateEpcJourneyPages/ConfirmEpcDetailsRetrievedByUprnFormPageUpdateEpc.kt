package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep

class ConfirmEpcDetailsRetrievedByUprnFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${ConfirmEpcRetrievedByUprnStep.ROUTE_SEGMENT}",
    )
