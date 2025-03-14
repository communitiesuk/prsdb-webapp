package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class OccupancyFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OccupancyForm(page)

    fun submitIsOccupied() {
        form.occupancyRadios.selectValue("true")
        form.submit()
    }

    fun submitIsVacant() {
        form.occupancyRadios.selectValue("false")
        form.submit()
    }

    class OccupancyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val occupancyRadios = Radios(locator)
    }
}
