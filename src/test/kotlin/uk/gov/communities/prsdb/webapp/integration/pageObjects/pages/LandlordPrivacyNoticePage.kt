package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordPrivacyNoticePage(
    page: Page,
) : BasePage(page, LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE) {
    val heading = Heading(page.locator("main h1"))
    val mhclgComplaintsLink = Link.byText(page, "make a complaint (opens in new tab)")
    val dataProtectionEmailLink = Link.byText(page, "dataprotection@communities.gov.uk")
    val icoLink = Link.byText(page, "www.ico.org.uk/ (opens in new tab)")
}
