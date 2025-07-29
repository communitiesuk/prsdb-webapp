package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class MeesExemptionCheckBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = MeesExemptionCheckForm(page)

    fun submitHasExemption() {
        form.propertyHasExemption.selectValue("true")
        form.submit()
    }

    fun submitDoesNotHaveExemption() {
        form.propertyHasExemption.selectValue("false")
        form.submit()
    }

    class MeesExemptionCheckForm(
        page: Page,
    ) : PostForm(page) {
        val propertyHasExemption = Radios(locator)
    }
}
