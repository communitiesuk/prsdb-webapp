package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LocalCouncilBetaFeedbackSuccessPage(
    page: Page,
) : BasePage(page, BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL)
