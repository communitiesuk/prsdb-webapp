package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class RentFrequencyFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val header = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("html"))

    val form = RentFrequencyForm(page)

    fun selectRentFrequency(rentFrequency: RentFrequency) {
        form.rentFrequencyRadios.selectValue(rentFrequency)
    }

    fun fillCustomRentFrequency(customRentFrequency: String) {
        form.customRentFrequencyInput.fill(customRentFrequency)
    }

    class RentFrequencyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val rentFrequencyRadios = Radios(locator)
        val customRentFrequencyInput = TextInput.textByFieldName(locator, "customRentFrequency")
    }
}
