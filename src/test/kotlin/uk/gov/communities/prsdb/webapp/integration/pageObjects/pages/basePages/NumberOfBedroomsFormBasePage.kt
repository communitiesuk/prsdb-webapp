package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NumberOfBedroomsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val backLink = BackLink.default(page)

    val sectionHeader = FormWithSectionHeader.SectionHeader(page.locator("html"))

    val form = NumberOfBedroomsForm(page)

    fun submitNumOfBedrooms(num: Int) = submitNumOfBedrooms(num.toString())

    private fun submitNumOfBedrooms(num: String) {
        form.numberOfBedroomsInput.fill(num)
        form.submit()
    }

    class NumberOfBedroomsForm(
        page: Page,
    ) : PostForm(page) {
        val numberOfBedroomsInput = TextInput.textByFieldName(locator, "numberOfBedrooms")
        val fieldsetLegend = FieldsetLegend(locator)
    }
}
