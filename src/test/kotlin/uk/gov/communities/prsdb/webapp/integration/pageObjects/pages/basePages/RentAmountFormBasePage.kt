package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class RentAmountFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val header = Heading(page.locator("h1"))
    val subheading = Heading(page.locator("h2"))
    val sectionHeader = SectionHeader(page.locator("html"))
    val billsExplanationForRentFrequency = Paragraph(page.getByTestId("bills-explanation-for-frequency"))
    val rentCalculationParagraph =
        Paragraph(
            page.getByText(
                "To work out the monthly equivalent, divide your rent amount by the number of months it covers. For example:",
            ),
        )

    val form = RentAmountForm(page)

    fun submitRentAmount(rentAmount: String) {
        form.rentAmountInput.fill(rentAmount)
        form.submit()
    }

    class RentAmountForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val rentAmountInput = TextInput.textByFieldName(locator, "rentAmount")
    }
}
