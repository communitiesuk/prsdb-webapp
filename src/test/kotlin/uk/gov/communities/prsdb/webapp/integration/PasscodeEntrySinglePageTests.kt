package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeEntryPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.services.PasscodeService

class PasscodeEntrySinglePageTests : SinglePageTestWithSeedData("data-mockuser-not-landlord.sql") {
    @MockitoBean
    lateinit var passcodeService: PasscodeService

    @Nested
    inner class PasscodeValidation {
        @Test
        fun `valid passcode submission redirects to landlord registration`(page: Page) {
            val validPasscode = "ABC123"
            whenever(passcodeService.isValidPasscode(validPasscode)).thenReturn(true)

            val passcodeEntryPage = navigator.goToPasscodeEntryPage()
            passcodeEntryPage.submitPasscode(validPasscode)

            // Should redirect to landlord registration name page
            assertPageIs(page, NameFormPageLandlordRegistration::class)
        }

        @Test
        fun `invalid passcode shows error message`(page: Page) {
            val invalidPasscode = "INVALID"
            whenever(passcodeService.isValidPasscode(invalidPasscode)).thenReturn(false)

            val passcodeEntryPage = navigator.goToPasscodeEntryPage()
            passcodeEntryPage.submitPasscode(invalidPasscode)

            // Should stay on same page with error
            assertPageIs(page, PasscodeEntryPage::class)
            assertThat(passcodeEntryPage.form.getErrorMessage()).containsText("Enter a valid passcode")
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "  ", "\t"])
        fun `blank passcode shows validation error`(
            blankPasscode: String,
            page: Page,
        ) {
            val passcodeEntryPage = navigator.goToPasscodeEntryPage()
            passcodeEntryPage.submitPasscode(blankPasscode)

            // Should stay on same page with validation error
            assertPageIs(page, PasscodeEntryPage::class)
            assertThat(passcodeEntryPage.form.getErrorMessage()).containsText("Enter a passcode")
        }

        @Test
        fun `submitting form without passcode shows validation error`(page: Page) {
            val passcodeEntryPage = navigator.goToPasscodeEntryPage()
            passcodeEntryPage.form.submit()

            // Should stay on same page with validation error
            assertPageIs(page, PasscodeEntryPage::class)
            assertThat(passcodeEntryPage.form.getErrorMessage()).containsText("Enter a passcode")
        }
    }
}
