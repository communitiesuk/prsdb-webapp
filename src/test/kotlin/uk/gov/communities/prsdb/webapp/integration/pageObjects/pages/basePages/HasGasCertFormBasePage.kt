package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class HasGasCertFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))

    val form = HasGasCertForm(page)

    fun submitHasCertificate() {
        form.hasCertRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoCertificate() {
        form.hasCertRadios.selectValue("false")
        form.submit()
    }

    class HasGasCertForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasCertRadios = Radios(locator)
    }
}
