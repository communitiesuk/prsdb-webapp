package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PageNotFoundPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.assertIsPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration

class LaUserRegistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("Test User")
            val nextPage = formPage.submit()
            val emailPage = assertIsPage(nextPage, EmailFormPageLaUserRegistration::class)
            assertThat(emailPage.fieldSetHeading).containsText("What is your work email address?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Navigating directly to this step redirects to the name step`() {
            val firstStep = navigator.skipToLaUserRegistrationEmailFormPage()
            assertThat(firstStep.fieldSetHeading).containsText("What is your full name?")
        }

        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("test@example.com")
            // This will need to change when the "check answers" page is implemented
            val nextPage = formPage.submit()
            val notFoundPage = assertIsPage(nextPage, PageNotFoundPage::class)
            assertThat(notFoundPage.heading).containsText("Page not found")
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText(
                "Enter a valid email address to continue. An email is required for contact purposes.",
            )
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("notAnEmail")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText("Enter an email address in the right format")
        }
    }
}
