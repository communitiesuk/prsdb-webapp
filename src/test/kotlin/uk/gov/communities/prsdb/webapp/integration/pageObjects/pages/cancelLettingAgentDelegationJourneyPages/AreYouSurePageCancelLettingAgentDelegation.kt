package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REMOVE_LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep

class AreYouSurePageCancelLettingAgentDelegation(
    page: Page,
) : BasePage(
        page,
        "/$REMOVE_LETTING_AGENT_PATH_SEGMENT/${AreYouSureStep.ROUTE_SEGMENT}",
    ) {
    val continueButton = Button.byText(page, "Continue")
    // TODO PDJB-1413: extend with any additional locators needed for full assertions
}
