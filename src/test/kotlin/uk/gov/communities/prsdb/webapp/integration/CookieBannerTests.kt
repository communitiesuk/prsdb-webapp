package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.CookieBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CookiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertTrue

class CookieBannerTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `Cookie banner can be used to accept cookies, then confirmation can be hidden`(browserContext: BrowserContext) {
        // Load dashboard page
        val dashboard = navigator.goToLandlordDashboard()
        BaseComponent.assertThat(dashboard.cookieBanner).isVisible()
        BaseComponent.assertThat(dashboard.cookieBanner).containsText("We’d also like to use analytics cookies")

        // Accept analytics cookies
        dashboard.cookieBanner.acceptCookiesButton.clickAndWait()
        assertTrue(browserContext.cookies().any { it.name == "cookie_consent" && it.value == "true" })
        BaseComponent.assertThat(dashboard.cookieBanner).containsText("You’ve accepted analytics cookies.")

        // Hide cookie banner
        dashboard.cookieBanner.hideConfirmationButton.clickAndWait()
        BaseComponent.assertThat(dashboard.cookieBanner).isHidden()
    }

    @Test
    fun `Cookie banner can be used to reject cookies, then confirmation has link to cookies page`(
        browserContext: BrowserContext,
        page: Page,
    ) {
        // Load dashboard page
        val dashboard = navigator.goToLandlordDashboard()
        BaseComponent.assertThat(dashboard.cookieBanner).isVisible()
        BaseComponent.assertThat(dashboard.cookieBanner).containsText("We’d also like to use analytics cookies")

        // Reject analytics cookies
        dashboard.cookieBanner.rejectCookiesButton.clickAndWait()
        assertTrue(browserContext.cookies().any { it.name == "cookie_consent" && it.value == "false" })
        BaseComponent.assertThat(dashboard.cookieBanner).containsText("You’ve rejected analytics cookies.")

        // Go to cookies page
        dashboard.cookieBanner.changeCookiesSettingLink.clickAndWait()
        assertPageIs(page, CookiesPage::class)
    }

    @Test
    fun `Cookie banner links to cookies page`(page: Page) {
        // Load dashboard page
        val dashboard = navigator.goToLandlordDashboard()
        BaseComponent.assertThat(dashboard.cookieBanner).isVisible()
        BaseComponent.assertThat(dashboard.cookieBanner).containsText("We’d also like to use analytics cookies")

        // Go to cookies page
        dashboard.cookieBanner.viewCookiesLink.clickAndWait()
        assertPageIs(page, CookiesPage::class)
    }

    @Test
    fun `Cookie banner is not shown on cookies page`(page: Page) {
        // Load cookies page
        val cookiesPage = navigator.goToCookiesPage()
        assertPageIs(page, CookiesPage::class)
        assertThat(cookiesPage.page.locator(CookieBanner.SELECTOR)).isHidden()
    }

    @Test
    fun `Cookie banner is not shown if consent cookie already exists`(page: Page) {
        // Accept analytics cookies
        var dashboard = navigator.goToLandlordDashboard()
        dashboard.cookieBanner.acceptCookiesButton.clickAndWait()

        // Load dashboard page
        dashboard = navigator.goToLandlordDashboard()
        BaseComponent.assertThat(dashboard.cookieBanner).isHidden()
    }
}
