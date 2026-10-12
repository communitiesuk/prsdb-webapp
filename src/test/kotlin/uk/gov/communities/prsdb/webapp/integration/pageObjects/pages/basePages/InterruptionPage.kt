package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm

abstract class InterruptionPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = PostForm(page)
    val heading = page.locator(".moj-interruption-card__heading")
    val body = page.locator(".moj-interruption-card__body")
    val submitButton = page.locator("button[type='submit']")
    val goBackLink = Link(page.locator(".govuk-link--inverse"))

    fun submit() = form.submit()

    // Steps that choose between interruption templates only override the title of the generic "change this?" variant
    protected fun titleHeadingIfConfirmingChange(titleHeading: String) =
        titleHeading.takeIf { heading.textContent().trim() == "Are you sure you want to change this?" }
}
