package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class EpcExemptionFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = EpcExemptionForm(page)

    fun submitExemptionReason(exemptionReason: EpcExemptionReason) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class EpcExemptionForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
