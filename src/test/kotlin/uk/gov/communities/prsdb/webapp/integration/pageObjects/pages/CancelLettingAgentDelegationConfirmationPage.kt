package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CancelLettingAgentDelegationConfirmationPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        CancelLettingAgentDelegationController.getRemoveLettingAgentConfirmationPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
        ),
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    // TODO PDJB-1413: extend with any additional locators needed for full assertions
}
