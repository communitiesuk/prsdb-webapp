package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType

abstract class FreeholdTypeFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = FreeholdTypeForm(page)
    val backLink = BackLink.default(page)

    fun submitFreeholdType(freeholdType: FreeholdType) {
        form.radios.selectValue(freeholdType)
        form.submit()
    }

    class FreeholdTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
