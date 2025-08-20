package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeLimitExceededPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ActiveProfiles("require-passcode")
class GeneratePasscodeTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoSpyBean
    lateinit var passcodeRepository: PasscodeRepository

    @Test
    fun `local authority admin can access generate passcode page from dashboard and navigate back`(page: Page) {
        // Navigate to generate passcode page from LA dashboard
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        dashboardPage.generatePasscodeLink.clickAndWait()
        val generatePasscodePage = assertPageIs(page, GeneratePasscodePage::class)

        // Verify passcode is displayed
        assertThat(generatePasscodePage.banner.title).containsText("New passcode")
        assert(generatePasscodePage.banner.passcode.isNotEmpty()) { "Passcode should be generated and displayed" }

        // Test return to dashboard link
        generatePasscodePage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LocalAuthorityDashboardPage::class)
    }

    @Test
    fun `refreshing the page gives the same passcode`(page: Page) {
        // Navigate to generate passcode page
        val generatePasscodePage = navigator.goToGeneratePasscodePage()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.banner.passcode
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Refresh the page
        page.reload()
        val refreshedPage = assertPageIs(page, GeneratePasscodePage::class)

        // Verify the same passcode is displayed
        val refreshedPasscode = refreshedPage.banner.passcode
        assertEquals(initialPasscode, refreshedPasscode)
    }

    @Test
    fun `clicking generate another passcode creates a new passcode`(page: Page) {
        // Navigate to generate passcode page
        val generatePasscodePage = navigator.goToGeneratePasscodePage()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.banner.passcode
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Click generate another passcode button
        generatePasscodePage.generateAnotherButton.clickAndWait()

        // Verify we're still on the generate passcode page
        val newGeneratePasscodePage = assertPageIs(page, GeneratePasscodePage::class)

        // Get the new passcode
        val newPasscode = newGeneratePasscodePage.banner.passcode
        assert(newPasscode.isNotEmpty()) { "New passcode should be generated" }
        assertNotEquals(initialPasscode, newPasscode)
    }

    @Test
    fun `exceeding maximum passcode limit redirects to error page`(page: Page) {
        // Mock the repository to return a count >= 1000 to trigger the limit exceeded condition
        whenever(passcodeRepository.count()).thenReturn(1000L)

        // Try to reach the generate passcode page from the LA dashboard
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        dashboardPage.generatePasscodeLink.clickAndWait()

        // Verify we're redirected to the passcode limit error page
        val errorPage = assertPageIs(page, PasscodeLimitExceededPage::class)
        assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }

    @Test
    fun `exceeding maximum passcode limit when generating new passcode redirects to error page`(page: Page) {
        // Navigate to generate passcode page first (this should work normally)
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        dashboardPage.generatePasscodeLink.clickAndWait()
        val generatePasscodePage = assertPageIs(page, GeneratePasscodePage::class)

        // Verify initial passcode is generated
        assert(generatePasscodePage.banner.passcode.isNotEmpty()) { "Initial passcode should be generated" }

        // Now mock the repository to return a count >= 1000 to trigger the limit for the next generation
        whenever(passcodeRepository.count()).thenReturn(1000L)

        // Click generate another passcode button
        generatePasscodePage.generateAnotherButton.clickAndWait()

        // Verify we're redirected to the passcode limit error page
        val errorPage = assertPageIs(page, PasscodeLimitExceededPage::class)
        assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }
}
