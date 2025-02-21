package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader

open class DeclarationBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = DeclarationBaseForm(page)

    fun agreeAndSubmit() {
        form.iAgreeCheckbox.check()
        form.submit()
    }

    class DeclarationBaseForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val iAgreeCheckbox = Checkboxes(locator).getCheckbox("true")
    }
}
