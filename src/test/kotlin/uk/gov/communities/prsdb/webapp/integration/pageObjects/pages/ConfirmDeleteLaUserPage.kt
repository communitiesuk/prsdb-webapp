package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DELETE_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Section
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmDeleteLaUserPage(
    page: Page,
) : BasePage(page, "/$DELETE_USER_PATH_SEGMENT/") {
    val userDetailsSection = Section.byTestId(page, "user-details-section")
    val form = PostForm(page)
}
