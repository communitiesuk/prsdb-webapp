package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link

abstract class NoAddressFoundFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = NoAddressFoundForm(page)
    val searchAgain = Link.byText(page, "search again")
    val heading: Heading = Heading(page.locator(".govuk-heading-l"))

    class NoAddressFoundForm(
        page: Page,
    ) : FormWithSectionHeader(page)
}
