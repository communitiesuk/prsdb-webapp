package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasElectricalCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

class HasElectricalCertFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : HasElectricalCertFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HasElectricalCertStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
    val provideThisLaterButton: Locator =
        page.locator("button[name='action'][value='$PROVIDE_THIS_LATER_BUTTON_ACTION_NAME']")
}
