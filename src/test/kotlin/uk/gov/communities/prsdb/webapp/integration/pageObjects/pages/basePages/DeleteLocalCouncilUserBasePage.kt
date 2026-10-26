package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Section

abstract class DeleteLocalCouncilUserBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val userDetailsSection = Section.byTestId(page, "user-details-section")
    val form = PostForm(page)

    override val expectedTitleHeading
        get() = "Before you remove ${page.locator("[data-testid=user-details-section] strong").first().textContent().trim()}"
}
