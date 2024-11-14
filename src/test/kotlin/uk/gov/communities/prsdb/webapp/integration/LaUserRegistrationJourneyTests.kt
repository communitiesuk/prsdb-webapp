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
        fun `Navigating directly to this step redirects to the name step`() {
            val firstStep = navigator.skipToLaUserRegistrationEmailFormPage()
            firstStep.assertHeadingContains("What is your full name?")
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
