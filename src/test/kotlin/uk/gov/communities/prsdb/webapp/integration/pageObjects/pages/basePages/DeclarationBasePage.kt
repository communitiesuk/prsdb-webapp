package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading

abstract class DeclarationBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = DeclarationBaseForm(page)

    val heading = Heading(page.locator("h1"))

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
