package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import kotlin.test.assertContains

abstract class FormBasePage(
    page: Page,
    private val urlSegment: String,
) : BasePage(page) {
    val form = Form(page)

    override fun validate() = assertContains(page.url(), urlSegment)

    fun submitInvalidForm() {
        form.getSubmitButton().click()
        page.waitForLoadState()
    }
}
