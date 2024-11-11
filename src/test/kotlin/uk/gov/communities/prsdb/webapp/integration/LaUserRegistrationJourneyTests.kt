package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LaUserRegistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillName("Test User")
            formPage.submit()
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillName("")
            formPage.submitUnsuccessfully()
            formPage.assertNameFormErrorContains("You must provide your name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Navigating directly to this step redirects to the name step`() {
            navigator.skipToLaUserRegistrationEmailFormPage()
        }

        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillEmail("test@example.com")
            formPage.submit()
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
