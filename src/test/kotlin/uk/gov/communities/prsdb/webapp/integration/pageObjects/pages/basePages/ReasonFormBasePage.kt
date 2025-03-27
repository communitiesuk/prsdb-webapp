package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea

open class ReasonFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = DeregistrationReasonForm(page)

    fun submitReason(reason: String) {
        form.textAreaInput.fill(reason)
        form.submit()
    }

    class DeregistrationReasonForm(
        page: Page,
    ) : Form(page) {
        val textAreaInput = TextArea.default(locator)
    }
}
