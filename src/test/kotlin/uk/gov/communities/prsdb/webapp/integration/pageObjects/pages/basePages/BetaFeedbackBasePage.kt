package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class BetaFeedbackBasePage(
    page: Page,
    url: String,
) : BasePage(page, url) {
    val form = BetaFeedbackForm(page)

    class BetaFeedbackForm(
        page: Page,
    ) : PostForm(page) {
        val feedbackInput = TextArea.default(locator)

        val emailInput = TextInput.emailByFieldName(locator, "email")

        val referrerInput = TextInput.hiddenByFieldName(locator, "referrerHeader")
    }
}
