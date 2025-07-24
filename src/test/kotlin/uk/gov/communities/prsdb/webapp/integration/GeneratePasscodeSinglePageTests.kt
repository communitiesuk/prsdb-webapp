package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeLimitExceededPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePasscodeSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @MockitoSpyBean
    lateinit var passcodeRepository: PasscodeRepository

    @Test
    fun `local authority admin can access generate passcode page from dashboard and navigate back`(page: Page) {
        // Navigate to LA dashboard
        val dashboardPage = navigator.goToLocalAuthorityDashboard()

        // Click generate passcode button to navigate to generate passcode page
        val generatePasscodePage = dashboardPage.clickGeneratePasscode()

        // Verify we're on the generate passcode page
        BasePage.assertPageIs(page, GeneratePasscodePage::class)
        // Use the specific confirmation panel heading from the page object
        BaseComponent.assertThat(generatePasscodePage.confirmationPanelHeading).containsText("Passcode generated")

        // Verify passcode is displayed
        val initialPasscode = generatePasscodePage.getPasscodeFromBanner()
        assert(initialPasscode.isNotEmpty()) { "Passcode should be generated and displayed" }

        // Test back button navigation
        generatePasscodePage.backLink.clickAndWait()
        BasePage.assertPageIs(page, LocalAuthorityDashboardPage::class)

        // Navigate back to generate passcode page
        val dashboardPageAgain = BasePage.assertPageIs(page, LocalAuthorityDashboardPage::class)
        val generatePasscodePageAgain = dashboardPageAgain.clickGeneratePasscode()

        // Test return to dashboard link
        val returnedDashboardPage = generatePasscodePageAgain.clickReturnToDashboard()
        BasePage.assertPageIs(page, LocalAuthorityDashboardPage::class)
    }

    @Test
    fun `refreshing the page gives the same passcode`(page: Page) {
        // Navigate to generate passcode page
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        val generatePasscodePage = dashboardPage.clickGeneratePasscode()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.getPasscodeFromBanner()
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Refresh the page
        page.reload()
        val refreshedPage = BasePage.assertPageIs(page, GeneratePasscodePage::class)

        // Verify the same passcode is displayed
        val refreshedPasscode = refreshedPage.getPasscodeFromBanner()
        assert(refreshedPasscode == initialPasscode) {
            "Refreshed passcode should be the same as initial passcode. Expected: $initialPasscode, but got: $refreshedPasscode"
        }
    }

    @Test
    fun `clicking generate another passcode creates a new passcode`(page: Page) {
        // Navigate to generate passcode page
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        val generatePasscodePage = dashboardPage.clickGeneratePasscode()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.getPasscodeFromBanner()
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Click generate another passcode button
        val newGeneratePasscodePage = generatePasscodePage.clickGenerateAnother()

        // Verify we're still on the generate passcode page
        BasePage.assertPageIs(page, GeneratePasscodePage::class)

        // Get the new passcode
        val newPasscode = newGeneratePasscodePage.getPasscodeFromBanner()
        assert(newPasscode.isNotEmpty()) { "New passcode should be generated" }
        assert(newPasscode != initialPasscode) {
            "New passcode should be different from initial passcode. Both were: $newPasscode"
        }
    }

    @Test
    fun `exceeding maximum passcode limit redirects to error page`(page: Page) {
        // Mock the repository to return a count >= 1000 to trigger the limit exceeded condition
        whenever(passcodeRepository.count()).thenReturn(1000L)

        // Navigate to LA dashboard
        val dashboardPage = navigator.goToLocalAuthorityDashboard()

        // Click generate passcode button using the page object method
        dashboardPage.clickGeneratePasscode()

        // Verify we're redirected to the passcode limit error page
        val errorPage = BasePage.assertPageIs(page, PasscodeLimitExceededPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }

    @Test
    fun `exceeding maximum passcode limit when generating new passcode redirects to error page`(page: Page) {
        // Navigate to generate passcode page first (this should work normally)
        val dashboardPage = navigator.goToLocalAuthorityDashboard()
        val generatePasscodePage = dashboardPage.clickGeneratePasscode()

        // Verify initial passcode is generated
        val initialPasscode = generatePasscodePage.getPasscodeFromBanner()
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Now mock the repository to return a count >= 1000 to trigger the limit for the next generation
        whenever(passcodeRepository.count()).thenReturn(1000L)

        // Click generate another passcode button
        generatePasscodePage.generateAnotherButton.clickAndWait()

        // Verify we're redirected to the passcode limit error page
        val errorPage = BasePage.assertPageIs(page, PasscodeLimitExceededPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }
}
