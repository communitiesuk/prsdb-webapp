package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class CertificateFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = CertificateForm(page)

    fun submitHasCert() {
        form.hasCertRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoCert() {
        form.hasCertRadios.selectValue("false")
        form.submit()
    }

    class CertificateForm(
        page: Page,
    ) : PostForm(page) {
        val hasCertRadios = Radios(locator)
    }
}
