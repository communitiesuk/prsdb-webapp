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

    fun selectSomethingElse() {
        form.billsIncludedCheckboxes.checkCheckbox(BillsIncluded.SOMETHING_ELSE.toString())
    }

    fun submitCustomBillsIncludedDetails(details: String) {
        form.customBillsIncludedTextArea.fill(details)
        form.submit()
    }

    class BillsIncludedForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val billsIncludedCheckboxes = Checkboxes(locator)
        val customBillsIncludedTextArea = TextArea.textByFieldName(locator, "customBillsIncluded")
    }
}
