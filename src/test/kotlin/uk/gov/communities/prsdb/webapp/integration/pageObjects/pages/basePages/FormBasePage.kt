package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form

abstract class FormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = Form(page)

    fun submitForm() {
        form.getSubmitButton().click()
        page.waitForLoadState()
    }
}
