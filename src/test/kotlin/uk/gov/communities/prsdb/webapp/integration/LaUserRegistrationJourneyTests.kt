package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.SummaryPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

class LaUserRegistrationJourneyTests : IntegrationTest() {
    @MockBean
    lateinit var invitationService: LocalAuthorityInvitationService

    @BeforeEach
    fun setup() {
        val testToken = "test token"
        whenever(invitationService.getTokenFromSession()).thenReturn(testToken)
        whenever(invitationService.tokenIsValid(testToken)).thenReturn(true)
    }

    @Nested
    inner class LaUserRegistrationLandingPage {
        @Test
        fun `Clicking submit redirects to the name step`(page: Page) {
            val landingPage = navigator.goToLaUserRegistrationLandingPage()
            landingPage.clickBeginButton()
            assertPageIs(page, NameFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`(page: Page) {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("Test User")
            namePage.form.submit()
            assertPageIs(page, EmailFormPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("")
            namePage.form.submit()
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Submitting a valid email redirects to the next step`(page: Page) {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("test@example.com")
            emailPage.form.submit()
            assertPageIs(page, SummaryPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText(
                "Enter a valid email address to continue. An email is required for contact purposes.",
            )
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("notAnEmail")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter an email address in the right format")
        }
    }
}
