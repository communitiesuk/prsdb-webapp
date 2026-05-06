package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasEpcFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep

class HasEpcFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : HasEpcFormBasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasEpcStep.ROUTE_SEGMENT}",
    ) {
    val provideThisLaterButton: Locator =
        page.locator("button[name='action'][value='$PROVIDE_THIS_LATER_BUTTON_ACTION_NAME']")
}
