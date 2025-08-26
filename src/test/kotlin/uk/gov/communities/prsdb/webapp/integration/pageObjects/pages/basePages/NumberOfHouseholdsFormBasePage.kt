package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form.FieldsetLegend
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NumberOfHouseholdsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val backLink = BackLink.default(page)

    val form = NumOfHouseholdsForm(page)

    fun submitNumberOfHouseholds(num: Int) = submitNumberOfHouseholds(num.toString())

    fun submitNumberOfHouseholds(num: String) {
        form.householdsInput.fill(num)
        form.submit()
    }

    class NumOfHouseholdsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val householdsInput = TextInput.textByFieldName(locator, "numberOfHouseholds")
        val fieldsetLegend = FieldsetLegend(locator)
    }
}
