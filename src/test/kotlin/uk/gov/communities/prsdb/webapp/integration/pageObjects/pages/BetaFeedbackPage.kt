package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class BetaFeedbackPage(
    page: Page,
) : BasePage(page, BetaFeedbackController.FEEDBACK_URL)
