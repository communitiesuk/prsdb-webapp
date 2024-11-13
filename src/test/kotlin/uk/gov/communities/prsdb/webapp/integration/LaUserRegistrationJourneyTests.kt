package uk.gov.communities.prsdb.webapp.integration

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
        fun `Click submit redirects to the name step`() {
            val formPage = navigator.goToLaUserRegistrationLandingPage()
            val nextStep = formPage.submit()
            nextStep.assertHeadingContains("What is your full name?")
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Navigating directly to this step redirects to the first step`() {
            val firstStep = navigator.skipToLaUserRegistrationNameFormPage()
            firstStep.assertHeadingContains("Registering as a local authority user")
        }

        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillName("Test User")
            val nextStep = formPage.submit()
            nextStep.assertHeadingContains("What is your work email address?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillName("")
            formPage.submitUnsuccessfully()
            formPage.assertNameFormErrorContains("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Navigating directly to this step redirects to the first step`() {
            val firstStep = navigator.skipToLaUserRegistrationEmailFormPage()
            firstStep.assertHeadingContains("Registering as a local authority user")
        }

        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillEmail("test@example.com")
            val nextStep = formPage.submit()
            // This will need to change when the "check answers" page is implemented
            nextStep.assertHeadingContains("Page not found")
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillEmail("")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillEmail("notAnEmail")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter an email address in the right format")
        }
    }
}
