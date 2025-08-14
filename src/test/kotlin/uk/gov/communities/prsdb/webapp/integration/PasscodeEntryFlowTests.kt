package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeAlreadyUsedPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeEntryPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.StartPageLandlordRegistration

@ActiveProfiles("require-passcode")
class PasscodeEntryFlowTests : IntegrationTestWithMutableData("data-passcode.sql") {
    @Test
    fun `passcode entry flow for logged out user (with unclaimed passcode)`(page: Page) {
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")

        assertPageIs(page, StartPageLandlordRegistration::class)

        // Once a passcode is in session, the user can access public pages without re-entering the passcode
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
        assertPageIs(page, StartPageLandlordRegistration::class)
    }

    @Test
    fun `passcode entry flow for logged out user (with claimed passcode)`(page: Page) {
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("TAKEN1")

        assertPageIs(page, StartPageLandlordRegistration::class)

        // Once a passcode is in session, the user can access public pages without re-entering the passcode
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
        assertPageIs(page, StartPageLandlordRegistration::class)
    }

    @Test
    fun `passcode entry flow for logged in user (with unclaimed passcode)`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)

        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")

        assertPageIs(page, LandlordDashboardPage::class)

        // Once a passcode is in session, the user can access restricted pages without re-entering the passcode
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)

        // Clear cookies to create a new session
        browserContext.clearCookies()

        // Once a passcode has been claimed, the user can access restricted pages without re-entering the passcode
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `passcode is claimed by user if login occurs after passcode entry ()`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("FREE01")

        assertPageIs(page, StartPageLandlordRegistration::class)

        // Access a restricted page, which logs the user in and claims the passcode
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)

        // Clear cookies to create a new session
        browserContext.clearCookies()

        // After clearing cookies, the user can still access the restricted page without re-entering the passcode
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `passcode in session was claimed by another user, user is redirected to passcode already claimed page after login`(page: Page) {
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        val passcodeEntryPage = assertPageIs(page, PasscodeEntryPage::class)
        passcodeEntryPage.submitPasscode("TAKEN1")

        // As page is public, user is not logged in, but passcode is stored in session
        assertPageIs(page, StartPageLandlordRegistration::class)

        // Now navigate to a restricted page, which logs the user in and triggers a check for whether they claimed the passcode
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, PasscodeAlreadyUsedPage::class)

        // The passcode in session is cleared by accessing the PasscodeAlreadyUsedPage
        navigator.navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
        val passcodeEntryPageAgain = assertPageIs(page, PasscodeEntryPage::class)
    }
}
