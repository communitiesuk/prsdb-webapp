package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

open class EpcLookupBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = EpcLookupForm(page)

    fun submitCurrentEpcNumber() {
        form.epcCertificateNumberInput.fill(CURRENT_EPC_CERTIFICATE_NUMBER)
        form.submit()
    }

    fun submitSupersededEpcNumber() {
        form.epcCertificateNumberInput.fill(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
        form.submit()
    }

    fun submitNonexistentEpcNumber() {
        form.epcCertificateNumberInput.fill(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
        form.submit()
    }

    fun submitInvalidEpcNumber() {
        form.epcCertificateNumberInput.fill(INVALID_EPC_CERTIFICATE_NUMBER)
        form.submit()
    }

    fun submitCurrentEpcNumberWhichIsExpired() {
        form.epcCertificateNumberInput.fill(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER)
        form.submit()
    }

    companion object {
        const val CURRENT_EPC_CERTIFICATE_NUMBER = "0000-0000-0000-0892-1563"

        const val CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER = "2758-7001-6218-6661-6024"

        const val SUPERSEDED_EPC_CERTIFICATE_NUMBER = "0000-0000-0000-0000-8410"

        const val NONEXISTENT_EPC_CERTIFICATE_NUMBER = "1234-0000-0000-0000-8410"

        const val INVALID_EPC_CERTIFICATE_NUMBER = "invalid-certificate-number"
    }

    class EpcLookupForm(
        page: Page,
    ) : Form(page) {
        val epcCertificateNumberInput = TextInput.textByFieldName(locator, "certificateNumber")
    }
}
