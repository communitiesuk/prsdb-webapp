package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.JourneyForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Section
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmDeleteLaUserPage(
    page: Page,
) : BasePage(page, "/delete-user/") {
    val userDetailsSection = Section.byTestId(page, "user-details-section")
    val form = JourneyForm(page)
}
