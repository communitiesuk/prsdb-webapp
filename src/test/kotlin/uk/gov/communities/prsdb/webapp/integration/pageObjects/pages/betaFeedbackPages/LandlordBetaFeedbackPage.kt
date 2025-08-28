package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BetaFeedbackBasePage

class LandlordBetaFeedbackPage(
    page: Page,
) : BetaFeedbackBasePage(page, BetaFeedbackController.LANDLORD_FEEDBACK_URL)
