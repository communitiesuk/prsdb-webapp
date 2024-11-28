package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getSection
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class ConfirmDeleteLaUserPage(
    page: Page,
) : BasePage(page, "/delete-user/") {
    val userDetailsSection = getSection(page)
    val form = Form(page)
}
