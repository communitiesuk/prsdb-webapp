package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class MeesExemptionReasonBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = MeesExemptionReasonForm(page)

    fun submitExemptionReason(exemptionReason: MeesExemptionReason) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class MeesExemptionReasonForm(
        page: Page,
    ) : PostForm(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
