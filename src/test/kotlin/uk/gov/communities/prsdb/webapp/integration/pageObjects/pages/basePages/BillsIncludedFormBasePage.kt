package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea

abstract class BillsIncludedFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = BillsIncludedForm(page)

    fun selectGasElectricityWater() {
        form.billsIncludedCheckboxes.checkCheckbox(
            BillsIncluded.GAS.toString(),
        )
        form.billsIncludedCheckboxes.checkCheckbox(
            BillsIncluded.ELECTRICITY.toString(),
        )
        form.billsIncludedCheckboxes.checkCheckbox(
            BillsIncluded.WATER.toString(),
        )
    }

    fun selectSomethingElseCheckbox() {
        form.billsIncludedCheckboxes.checkCheckbox(BillsIncluded.SOMETHING_ELSE.toString())
    }

    fun fillCustomBills(details: String) {
        form.customBillsIncludedTextArea.fill(details)
    }

    class BillsIncludedForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val billsIncludedCheckboxes = Checkboxes(locator)
        val customBillsIncludedTextArea = TextArea.textByFieldName(locator, "customBillsIncluded")
    }
}
