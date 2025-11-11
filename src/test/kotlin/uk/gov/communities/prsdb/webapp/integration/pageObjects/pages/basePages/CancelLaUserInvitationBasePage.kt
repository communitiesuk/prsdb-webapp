package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Section

abstract class CancelLaUserInvitationBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val userDetailsSection = Section.byTestId(page, "user-details-section")
    val form = PostForm(page)
}
