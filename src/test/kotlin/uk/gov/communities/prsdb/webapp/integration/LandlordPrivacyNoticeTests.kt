package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLAINTS_PROCEDURE_URL
import uk.gov.communities.prsdb.webapp.constants.INFORMATION_COMMISSIONERS_OFFICE_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordPrivacyNoticePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class LandlordPrivacyNoticeTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `page renders correctly`(page: Page) {
        navigator.goToLandlordPrivacyNoticePage()
        val privacyNoticePage = assertPageIs(page, LandlordPrivacyNoticePage::class)
        assertThat(privacyNoticePage.heading).containsText("Privacy notice")
    }

    @Test
    fun `the complaints link opens a new tab with the correct external url`(
        browserContext: BrowserContext,
        page: Page,
    ) {
        val privacyNoticePage = navigator.goToLandlordPrivacyNoticePage()
        assertThat(privacyNoticePage.mhclgComplaintsLink).hasAttribute("href", COMPLAINTS_PROCEDURE_URL)
        assertThat(privacyNoticePage.mhclgComplaintsLink).hasAttribute("rel", "noreferrer noopener")
        assertThat(privacyNoticePage.mhclgComplaintsLink).hasAttribute("target", "_blank")
    }

    @Test
    fun `the data protection email link is a mailto link for the correct email address`(page: Page) {
        val privacyNoticePage = navigator.goToLandlordPrivacyNoticePage()
        assertThat(privacyNoticePage.dataProtectionEmailLink)
            .hasAttribute("href", "mailto:dataprotection@communities.gov.uk")
    }

    @Test
    fun `the ICO link opens a new tab with the correct external url`(
        browserContext: BrowserContext,
        page: Page,
    ) {
        val privacyNoticePage = navigator.goToLandlordPrivacyNoticePage()
        assertThat(privacyNoticePage.icoLink).hasAttribute("href", INFORMATION_COMMISSIONERS_OFFICE_URL)
        assertThat(privacyNoticePage.icoLink).hasAttribute("rel", "noreferrer noopener")
        assertThat(privacyNoticePage.icoLink).hasAttribute("target", "_blank")
    }
}
