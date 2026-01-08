package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.UnorderedList

abstract class RentAmountFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val header = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("html"))
    val billsExplanationForRentFrequencyBullet = UnorderedList(page.getByTestId("rent-includes-list")).elements[2]
    val rentCalculationSubHeading = Heading(page.getByText("How to calculate the rent charge"))

    val form = RentAmountForm(page)

    fun fillRentAmount(rentAmount: String) {
        form.rentAmountInput.fill(rentAmount)
    }

    class RentAmountForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val rentAmountInput = TextInput.textByFieldName(locator, "rentAmount")
    }
}
