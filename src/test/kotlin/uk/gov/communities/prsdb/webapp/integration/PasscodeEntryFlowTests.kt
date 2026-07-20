package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InvalidPasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
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
    fun `Restricted landlord pages outside registration do not require a passcode`(page: Page) {
        // The landlord dashboard is no longer passcode-gated - it is protected by role-based auth only
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `Logged in users claim a passcode by accessing the registration journey`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        // Authenticated user submits a valid passcode within the gated registration journey, claiming it
        authenticateThenSubmitPasscodeOnRegistrationJourney(page, "FREE01")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // Clear cookies to create a new session
        browserContext.clearCookies()

        // Re-authenticate; as the passcode is claimed, the user reaches the gated registration
        // journey without being sent back to passcode entry
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)
        navigator.navigateToLandlordRegistrationStartPage()
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)
    }

    @Test
    fun `Users are sent to the invalid passcode page when an already claimed passcode is validated on login`(page: Page) {
        // TAKEN1 is already claimed by another user, so validating it as an authenticated user fails
        authenticateThenSubmitPasscodeOnRegistrationJourney(page, "TAKEN1")
        val invalidPasscodePage = assertPageIs(page, InvalidPasscodePage::class)
        invalidPasscodePage.goBackLink.clickAndWait()
        assertPageIs(page, PasscodeEntryPage::class)

        // Accessing the invalid passcode page clears the passcode from session
        navigator.navigateToLandlordRegistrationStartPage()
        assertPageIs(page, PasscodeEntryPage::class)
    }

    @Test
    fun `Users are still redirected to their original previous page after being sent to the invalid passcode page`(page: Page) {
        // Authenticate, then hit the gated registration journey so the original destination is remembered
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)

        navigator.navigateToLandlordRegistrationStartPage()
        var passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)

        // An already claimed passcode sends the authenticated user to the invalid passcode page
        passcodeEntryPage.submitPasscode("TAKEN1")
        val invalidPasscodePage = assertPageIs(page, InvalidPasscodePage::class)
        invalidPasscodePage.goBackLink.clickAndWait()
        passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)

        // Submitting a valid, unclaimed passcode redirects to the original registration page
        passcodeEntryPage.submitPasscode("FREE01")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)
    }

    @Test
    fun `Users who have claimed a passcode can't access passcode pages`(page: Page) {
        // Claim a passcode via the registration journey
        authenticateThenSubmitPasscodeOnRegistrationJourney(page, "FREE01")
        assertPageIs(page, ServiceInformationStartPageLandlordRegistration::class)

        // Try to access passcode pages
        navigator.navigateToPasscodeEntryPage()
        assertPageIs(page, LandlordDashboardPage::class)

        navigator.navigateToInvalidPasscodePage()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    private fun authenticateThenSubmitPasscodeOnRegistrationJourney(
        page: Page,
        passcode: String,
    ) {
        // Not a real user flow: a real user authenticates via One Login and would not reach the
        // dashboard before registering. Under the local-no-auth test profile, visiting an
        // authenticated route (the dashboard) simply logs the mock user in, which is all we need here.
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)

        // The registration journey is gated, so the authenticated user is sent to passcode entry
        navigator.navigateToLandlordRegistrationStartPage()
        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode(passcode)
    }
}
