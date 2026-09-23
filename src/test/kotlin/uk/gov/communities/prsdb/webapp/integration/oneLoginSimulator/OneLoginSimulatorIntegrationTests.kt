package uk.gov.communities.prsdb.webapp.integration.oneLoginSimulator

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OneLoginSimulatorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OneLoginSimulatorIntegrationTests : OneLoginSimulatorIntegrationTestBase() {
    @Test
    fun `seeded landlord can authenticate through the official simulator`(page: Page) {
        page.navigate("http://localhost:$port/oauth2/authorization/one-login")
        assertTrue(page.url().startsWith(simulator.baseUrl), "Expected simulator page but was ${page.url()}")

        val simulatorPage = OneLoginSimulatorPage(page)
        simulatorPage.submitSubject(SEEDED_LANDLORD_SUBJECT)

        val dashboard = BasePage.Companion.assertPageIs(page, LandlordDashboardPage::class)
        val heading = dashboard.dashboardBannerHeading
        assertTrue(heading.getText().contains("Alexander Smith"))
    }

    @Test
    fun `identity verification authorization request includes the expected claims`(page: Page) {
        page.navigate("http://localhost:$port/id-verification/oauth2/authorize/one-login")
        assertTrue(page.url().startsWith(simulator.baseUrl), "Expected simulator page but was ${page.url()}")

        val simulatorPage = OneLoginSimulatorPage(page)

        assertEquals("[\"Cl.Cm.P2\"]", simulatorPage.vtrValue)
        assertTrue(simulatorPage.claimsValue.contains("https://vocab.account.gov.uk/v1/coreIdentityJWT"))
        assertTrue(simulatorPage.claimsValue.contains("https://vocab.account.gov.uk/v1/address"))
        assertTrue(simulatorPage.claimsValue.contains("https://vocab.account.gov.uk/v1/returnCode"))

        simulatorPage.submitIdentityVerificationFixture(
            subject = SEEDED_LANDLORD_SUBJECT,
            coreIdentity = readResource("one-login-simulator/core-identity.json"),
            address = readResource("one-login-simulator/address.json"),
            returnCodes = readResource("one-login-simulator/return-codes.json"),
        )

        page.waitForURL("http://localhost:$port/**")
        navigator.navigateToLandlordDashboard()
        BasePage.Companion.assertPageIs(page, LandlordDashboardPage::class)
    }

    companion object {
        private const val SEEDED_LANDLORD_SUBJECT = "urn:fdc:gov.uk:2022:UVWXY"

        private fun readResource(path: String): String {
            return ClassLoader.getSystemResource(path)?.readText() ?: error("Missing resource $path")
        }
    }
}
