package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.delegateToLettingAgentJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig.AllowLettingAgentStep

class AllowLettingAgentPageDelegateToLettingAgent(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DelegateToLettingAgentController.getDelegateToLettingAgentBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${AllowLettingAgentStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val continueButton = Button.byText(page, "Continue")
    val backLink = BackLink.default(page)

    fun submitContinue() {
        continueButton.clickAndWait()
    }
}
