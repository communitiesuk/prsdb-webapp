package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class SuperiorLandlordContactTypeFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = SuperiorLandlordContactTypeForm(page)
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)

    fun submitContactType(contactType: SuperiorLandlordContactType) {
        form.radios.selectValue(contactType)
        form.submit()
    }

    class SuperiorLandlordContactTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
