package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
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
        fun `Clicking submit redirects to the name step`() {
            val landingPage = navigator.goToLaUserRegistrationLandingPage()
            landingPage.clickBeginAndAssertNextPage()
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("Test User")
            namePage.submitFormAndAssertNextPage()
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("")
            namePage.submitForm()
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("test@example.com")
            emailPage.submitFormAndAssertNextPage()
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.submitForm()
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
            emailPage.submitForm()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter an email address in the right format")
        }
    }
}
