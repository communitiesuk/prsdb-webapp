package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilPrivacyNoticePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class LocalCouncilPrivacyNoticeTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `page renders correctly`(page: Page) {
        navigator.goToLocalCouncilPrivacyNoticePage()
        val privacyNoticePage = assertPageIs(page, LocalCouncilPrivacyNoticePage::class)
        assertThat(privacyNoticePage.heading).containsText("Privacy notice")
    }

    @Test
    fun `the back link is not shown when navigated to directly`(page: Page) {
        val privacyNoticePage = navigator.goToLocalCouncilPrivacyNoticePage()
        assertThat(privacyNoticePage.backLink).isHidden()
    }
}
