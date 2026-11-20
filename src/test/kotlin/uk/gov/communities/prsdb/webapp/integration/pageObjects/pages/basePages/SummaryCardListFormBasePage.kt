package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard

abstract class SummaryCardListFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)
    val addAnotherButton: Locator = page.locator("a.govuk-button--secondary")
    val backLink = BackLink.default(page)

    fun getSummaryCard(index: Int) = SummaryCard(page, index)

    fun submit() = form.submit()
}
