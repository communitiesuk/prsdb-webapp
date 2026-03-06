package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NumberOfBedroomsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val backLink = BackLink.default(page)
    val header = Heading(page.locator("h1"))

    val form = NumberOfBedroomsForm(page)

    fun submitNumOfBedrooms(num: Int) = submitNumOfBedrooms(num.toString())

    fun submitNumOfBedrooms(num: String) {
        form.numberOfBedroomsInput.fill(num)
        form.submit()
    }

    class NumberOfBedroomsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val numberOfBedroomsInput = TextInput.textByFieldName(locator, "numberOfBedrooms")
        val fieldsetLegend = FieldsetLegend(locator)
    }
}
