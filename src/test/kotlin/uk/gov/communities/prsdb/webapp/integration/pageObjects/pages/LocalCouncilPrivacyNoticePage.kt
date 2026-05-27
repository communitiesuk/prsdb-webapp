package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilPrivacyNoticeController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LocalCouncilPrivacyNoticePage(
    page: Page,
) : BasePage(page, LocalCouncilPrivacyNoticeController.LOCAL_COUNCIL_PRIVACY_NOTICE_ROUTE) {
    val backLink = BackLink.default(page)
    val heading = Heading(page.locator("main h1"))
}
