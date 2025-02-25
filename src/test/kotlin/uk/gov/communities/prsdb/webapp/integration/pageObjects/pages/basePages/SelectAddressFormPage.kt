package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class SelectAddressFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = SelectAddressForm(page)
    val searchAgain = Link.byText(page, "Search Again")

    fun selectAddressAndSubmit(address: String) {
        form.addressRadios.selectValue(address)
        form.submit()
    }

    class SelectAddressForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val addressRadios = Radios(locator)
    }
}
