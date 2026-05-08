package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class HasMeesExemptionFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = HasMeesExemptionForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    fun submitHasMeesExemption() {
        form.hasMeesExemptionRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoMeesExemption() {
        form.hasMeesExemptionRadios.selectValue("false")
        form.submit()
    }

    class HasMeesExemptionForm(
        page: Page,
    ) : PostForm(page) {
        val hasMeesExemptionRadios = Radios(locator)
    }
}
