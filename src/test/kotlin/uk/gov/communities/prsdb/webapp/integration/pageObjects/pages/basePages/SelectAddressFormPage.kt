package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link

abstract class SelectAddressFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val searchAgain = Link.byText(page, "Search Again")
    val radios = form.getRadios()
}
