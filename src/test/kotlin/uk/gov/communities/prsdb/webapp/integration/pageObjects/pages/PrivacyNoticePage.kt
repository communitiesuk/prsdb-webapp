package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PrivacyNoticePage(
    page: Page,
) : BasePage(page, "/$PRIVACY_NOTICE_PATH_SEGMENT") {
    val backLink = BackLink.default(page)
    val heading = Heading(page.locator("main h1"))
}
