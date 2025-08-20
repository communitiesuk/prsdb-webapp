package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeAlreadyUsedPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeEntryPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ServiceInformationStartPageLandlordRegistration

@ActiveProfiles("require-passcode")
class PasscodeEntryFlowTests : IntegrationTestWithMutableData("data-passcode.sql") {
    @Test
    fun `Passcode entry is not required for non-landlord pages`(page: Page) {
        val cookiesPage = navigator.goToCookiesPage()
        assertThat(cookiesPage.page.url()).doesNotContain(LANDLORD_PATH_SEGMENT)
    }

    @Test
    fun `Logged out users can access public pages with an unclaimed passcode`(page: Page) {
        navigator.navigateToLandlordRegistrationStartPage()

        // Store submitted passcode in session and redirect to previous page
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // As passcode is in session, the user can access public pages without re-entering it
        navigator.navigateToLandlordRegistrationStartPage()
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)
    }

    @Test
    fun `Logged out users can access public pages with a claimed passcode`(page: Page) {
        navigator.navigateToLandlordRegistrationStartPage()

        // Store submitted passcode in session and redirect to previous page
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("TAKEN1")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // As passcode is in session, the user can access public pages without re-entering it
        navigator.navigateToLandlordRegistrationStartPage()
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)
    }

    @Test
    fun `Logged in users can access restricted pages by claiming a passcode`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        navigator.navigateToLandlordDashboard()

        // Store submitted passcode in session and redirect to previous page
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")

        // Claim the passcode when the user is redirected to a restricted page
        assertPageIs(page, LandlordDashboardPage::class)

        // Clear cookies to create a new session
        browserContext.clearCookies()

        // As the user has claimed a passcode, they can still access restricted pages without re-entering it
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `Users can claim a submitted passcode by logging in`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        navigator.navigateToLandlordRegistrationStartPage()

        // Store submitted passcode in session and redirect to previous page
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // Access a restricted page, which logs the user in and claims the passcode
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)

        // Clear cookies to create a new session
        browserContext.clearCookies()

        // As the user has claimed a passcode, they can still access restricted pages without re-entering it
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `Users are redirected to passcode already claimed page after login if they submit an already claimed passcode`(page: Page) {
        navigator.navigateToLandlordRegistrationStartPage()

        // Store submitted passcode in session and redirect to previous page
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("TAKEN1")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // Access a restricted page, which logs the user in and determines that the passcode was claimed by another user
        navigator.navigateToLandlordDashboard()
        val passcodeUsedPage = assertPageIs(page, PasscodeAlreadyUsedPage::class)
        passcodeUsedPage.tryAnotherPasscodeButton.clickAndWait()
        assertPageIs(page, PasscodeEntryPage::class)

        // Accessing the PasscodeAlreadyUsedPage clears the passcode from session
        navigator.navigateToLandlordRegistrationStartPage()
        assertPageIs(page, PasscodeEntryPage::class)
    }
}
