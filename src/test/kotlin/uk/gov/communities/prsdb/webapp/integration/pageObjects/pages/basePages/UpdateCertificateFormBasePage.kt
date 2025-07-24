package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class UpdateCertificateFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = UpdateCertificateForm(page)

    fun submitHasNewCertificate() {
        form.hasNewCertificateRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNewExemption() {
        form.hasNewCertificateRadios.selectValue("false")
        form.submit()
    }

    class UpdateCertificateForm(
        page: Page,
    ) : PostForm(page) {
        val hasNewCertificateRadios = Radios(locator)
    }
}
