package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios

open class PageWithYesNoRadios(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = FormWithRadios(page)

    fun submitYes() {
        form.radios.selectValue("true")
        form.submit()
    }

    fun submitNo() {
        form.radios.selectValue("false")
        form.submit()
    }
}
