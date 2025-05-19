package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.JourneyForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea

open class ReasonFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = ReasonForm(page)

    fun submitReason(reason: String) {
        form.textAreaInput.fill(reason)
        form.submit()
    }

    class ReasonForm(
        page: Page,
    ) : JourneyForm(page) {
        val textAreaInput = TextArea.default(locator)
    }
}
