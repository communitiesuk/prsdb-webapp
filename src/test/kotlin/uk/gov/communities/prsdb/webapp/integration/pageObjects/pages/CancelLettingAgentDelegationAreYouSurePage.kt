package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CancelLettingAgentDelegationAreYouSurePage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        CancelLettingAgentDelegationController.getRemoveLettingAgentPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    val continueButton = Button.byText(page, "Continue")
    // TODO PDJB-1413: extend with any additional locators needed for full assertions
}
