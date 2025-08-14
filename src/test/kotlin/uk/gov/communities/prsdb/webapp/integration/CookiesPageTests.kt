package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.Cookie
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CookiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CookiesPageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @ParameterizedTest(name = "{1} when consent cookie {0}")
    @MethodSource("provideFormPopulationCases")
    fun `Consent form is pre-populated with`(
        consentCookieValue: Boolean?,
        expectedPrepopulatedValue: String,
        browserContext: BrowserContext,
    ) {
        if (consentCookieValue != null) {
            browserContext.addCookies(
                listOf(
                    Cookie("cookie_consent", consentCookieValue.toString()).apply {
                        setDomain("localhost")
                        setPath("/")
                    },
                ),
            )
        }

        val cookiesPage = navigator.goToCookiesPage()
        assertThat(cookiesPage.consentForm).isVisible()
        assertEquals(expectedPrepopulatedValue, cookiesPage.consentForm.radios.selectedValue)
    }

    @Test
    fun `Consent form can be used to accept cookies, then users can return to the previous page via the backlink`(
        browserContext: BrowserContext,
        page: Page,
    ) {
        // Go to cookies page from non-landing page
        val landlordDetailsPage = navigator.goToLandlordDetails()
        landlordDetailsPage.cookieBanner.viewCookiesLink.clickAndWait()

        // Consent to cookies
        val cookiesPage = assertPageIs(page, CookiesPage::class)
        cookiesPage.consentForm.radios.selectValue("true")
        cookiesPage.consentForm.submit()
        assertTrue(browserContext.cookies().any { it.name == "cookie_consent" && it.value == "true" })
        assertThat(cookiesPage.successBanner).isVisible()

        // Go back to the landlord details page
        cookiesPage.backLink.clickAndWait()
        assertPageIs(page, LandlordDetailsPage::class)
    }

    @Test
    fun `Consent form can be used to reject cookies, then users can return to the previous page via the success banner's link`(
        browserContext: BrowserContext,
        page: Page,
    ) {
        // Go to cookies page from non-landing page
        val landlordDetailsPage = navigator.goToLandlordDetails()
        landlordDetailsPage.cookieBanner.viewCookiesLink.clickAndWait()

        // Reject cookies
        val cookiesPage = assertPageIs(page, CookiesPage::class)
        cookiesPage.consentForm.radios.selectValue("false")
        cookiesPage.consentForm.submit()
        assertTrue(browserContext.cookies().any { it.name == "cookie_consent" && it.value == "false" })
        assertThat(cookiesPage.successBanner).isVisible()

        // Go back to the landlord details page
        cookiesPage.successBanner.backLink.clickAndWait()
        assertPageIs(page, LandlordDetailsPage::class)
    }

    companion object {
        @JvmStatic
        fun provideFormPopulationCases() =
            listOf(
                arguments(named("is not set", null), named("No", "false")),
                arguments(named("is set to false", false), named("No", "false")),
                arguments(named("is set to true", true), named("Yes", "true")),
            )
    }
}
