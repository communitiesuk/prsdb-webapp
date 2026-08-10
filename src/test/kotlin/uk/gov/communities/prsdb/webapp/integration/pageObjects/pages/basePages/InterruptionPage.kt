package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm

abstract class InterruptionPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = PostForm(page)

    fun submit() = form.submit()
}
