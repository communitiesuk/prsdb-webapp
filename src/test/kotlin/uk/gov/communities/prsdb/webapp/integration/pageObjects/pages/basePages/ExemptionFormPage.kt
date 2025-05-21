package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class ExemptionFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = ExemptionForm(page)

    fun submitHasExemption() {
        form.hasExemptionRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoExemption() {
        form.hasExemptionRadios.selectValue("false")
        form.submit()
    }

    class ExemptionForm(
        page: Page,
    ) : PostForm(page) {
        val hasExemptionRadios = Radios(locator)
    }
}
