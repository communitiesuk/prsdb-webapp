package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertTrue
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
        val newPage =
            browserContext.waitForPage {
                privacyNoticePage.mhclgComplaintsLink.clickAndWait()
            }
        assertTrue(newPage.url().contains(COMPLAINTS_PROCEDURE_URL))
    }

    @Test
    fun `the data protection email link opens the email client`(
        browserContext: BrowserContext,
        page: Page,
    ) {
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
        val newPage =
            browserContext.waitForPage {
                privacyNoticePage.icoLink.clickAndWait()
            }
        assertTrue(newPage.url().contains(INFORMATION_COMMISSIONERS_OFFICE_URL))
    }
}
