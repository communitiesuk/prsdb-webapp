package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class ExemptionReasonFormPage<ReasonType : Enum<ReasonType>>(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = ExemptionReasonForm(page)

    fun submitExemptionReason(exemptionReason: ReasonType) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class ExemptionReasonForm(
        page: Page,
    ) : PostForm(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
