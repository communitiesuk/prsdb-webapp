package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class HasGasSupplyFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = HasGasSupplyForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    fun submitHasGasSupply() {
        form.hasGasSupplyRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoGasSupply() {
        form.hasGasSupplyRadios.selectValue("false")
        form.submit()
    }

    class HasGasSupplyForm(
        page: Page,
    ) : PostForm(page) {
        val hasGasSupplyRadios = Radios(locator)
    }
}
