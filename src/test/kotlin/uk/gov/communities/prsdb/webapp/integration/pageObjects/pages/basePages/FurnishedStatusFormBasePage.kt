package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class FurnishedStatusFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = FurnishedForm(page)

    fun submitFurnishedStatus(furnishedStatus: FurnishedStatus) {
        form.furnishedRadios.selectValue(furnishedStatus)
        form.submit()
    }

    class FurnishedForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val furnishedRadios = Radios(page)
    }
}
