package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REMOVE_LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep

class AreYouSurePageCancelLettingAgentDelegation(
    page: Page,
) : AreYouSureFormBasePage(
        page,
        "/$REMOVE_LETTING_AGENT_PATH_SEGMENT/${AreYouSureStep.ROUTE_SEGMENT}",
    )
