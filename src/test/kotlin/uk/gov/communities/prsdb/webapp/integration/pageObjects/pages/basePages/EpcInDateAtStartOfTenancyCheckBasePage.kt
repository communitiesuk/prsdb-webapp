package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class EpcInDateAtStartOfTenancyCheckBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val bodyParagraph = page.locator("p.govuk-body").first()
    val form = EpcInDateAtStartOfTenancyCheckForm(page)

    fun submitEpcInDate() {
        form.epcInDateAtStartOfTenancyRadios.selectValue("true")
        form.submit()
    }

    fun submitEpcExpired() {
        form.epcInDateAtStartOfTenancyRadios.selectValue("false")
        form.submit()
    }

    class EpcInDateAtStartOfTenancyCheckForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val epcInDateAtStartOfTenancyRadios = Radios(locator)
        val yesHint = locator.locator(".govuk-radios__hint")
    }
}
