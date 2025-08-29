package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BetaFeedbackBasePage

class LocalCouncilBetaFeedbackPage(
    page: Page,
) : BetaFeedbackBasePage(page, BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL)
