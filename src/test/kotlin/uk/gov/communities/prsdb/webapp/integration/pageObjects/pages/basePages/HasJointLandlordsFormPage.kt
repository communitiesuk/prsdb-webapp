package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class HasJointLandlordsFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = HasJointLandlordsForm(page)

    val header = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("html"))
    val legalAdviceLink = Link.byText(page, "Find legal advice and information (opens in new tab)")

    fun submitHasJointLandlords() {
        form.hasJointLandlordsRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoJointLandlords() {
        form.hasJointLandlordsRadios.selectValue("false")
        form.submit()
    }

    class HasJointLandlordsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasJointLandlordsRadios = Radios(locator)
    }
}
