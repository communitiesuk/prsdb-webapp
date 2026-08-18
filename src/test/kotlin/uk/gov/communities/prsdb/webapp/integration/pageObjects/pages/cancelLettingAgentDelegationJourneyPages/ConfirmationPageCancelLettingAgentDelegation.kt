package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REMOVE_LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageCancelLettingAgentDelegation(
    page: Page,
) : BasePage(
        page,
        "/$REMOVE_LETTING_AGENT_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val continueButton = Button.byText(page, "Continue")
    // TODO PDJB-1414: extend with any additional locators needed for full assertions
}
